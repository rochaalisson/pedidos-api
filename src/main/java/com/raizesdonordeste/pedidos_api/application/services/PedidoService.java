package com.raizesdonordeste.pedidos_api.application.services;

import com.raizesdonordeste.pedidos_api.api.dto.AtualizarStatusRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.PedidoListagemResponseDTO;
import com.raizesdonordeste.pedidos_api.api.dto.PedidoRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.PedidoResponseDTO;
import com.raizesdonordeste.pedidos_api.api.exceptions.NotFoundException;
import com.raizesdonordeste.pedidos_api.api.exceptions.RegraNegocioException;
import com.raizesdonordeste.pedidos_api.domain.enums.CanalPedido;
import com.raizesdonordeste.pedidos_api.domain.enums.StatusPedido;
import com.raizesdonordeste.pedidos_api.domain.models.*;
import com.raizesdonordeste.pedidos_api.infrastructure.external.PagamentoMockClient;
import com.raizesdonordeste.pedidos_api.infrastructure.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoUnidadeRepository estoqueRepository;
    private final ProdutoPedidoRepository produtoPedidoRepository;
    private final UnidadeRepository unidadeRepository;
    private final ClienteRepository clienteRepository;
    private final PagamentoMockClient pagamentoClient;

    public List<PedidoListagemResponseDTO> listar(CanalPedido canal) {
        List<Pedido> pedidos = canal != null
                ? pedidoRepository.findByCanal(canal)
                : pedidoRepository.findAll();

        return pedidos.stream().map(p -> new PedidoListagemResponseDTO(
                p.getId(),
                p.getStatus().name(),
                p.getCanal().name(),
                p.getValorTotal(),
                p.getDataCriacao(),
                p.getUnidade().getId(),
                p.getCliente() != null ? p.getCliente().getId() : null,
                p.getUsuario() != null ? p.getUsuario().getId() : null
        )).toList();
    }

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO request) {
        Unidade unidade = unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> new NotFoundException("Unidade não encontrada"));

        Pedido pedido = Pedido.builder()
                .unidade(unidade)
                .canal(request.canal())
                .status(StatusPedido.AGUARDANDO_PAGAMENTO)
                .valorTotal(BigDecimal.ZERO)
                .dataCriacao(OffsetDateTime.now())
                .build();

        List<ProdutoPedido> itensPedido = new ArrayList<>();
        BigDecimal totalCalculado = BigDecimal.ZERO;

        for (PedidoRequestDTO.ItemRequestDTO itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                    .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + itemRequest.produtoId()));

            ProdutoUnidade estoque = estoqueRepository.findByUnidadeIdAndProdutoId(unidade.getId(), produto.getId())
                    .orElseThrow(() -> new RegraNegocioException("Produto não disponível nesta unidade"));

            verificarDisponibilidade(estoque, itemRequest.quantidade());

            estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() - itemRequest.quantidade());
            estoqueRepository.save(estoque);

            ProdutoPedido item = ProdutoPedido.builder()
                    .pedido(pedido)
                    .produto(produto)
                    .quantidade(itemRequest.quantidade())
                    .precoUnitario(produto.getPreco())
                    .build();

            itensPedido.add(item);

            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemRequest.quantidade()));
            totalCalculado = totalCalculado.add(subtotal);
        }

        Cliente cliente = null;

        if (request.clienteId() != null) {
            cliente = clienteRepository.findById(request.clienteId())
                    .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

            pedido.setCliente(cliente);
        }

        if (Boolean.TRUE.equals(request.usarPontos())) {

            if (cliente == null) {
                throw new RegraNegocioException("É necessário informar um cliente para utilizar pontos.");
            }

            if (!cliente.getAceitaProgramaFidelidade()) {
                throw new RegraNegocioException("Cliente não participa do programa de fidelidade.");
            }

            BigDecimal desconto = BigDecimal.valueOf(cliente.getPontosFidelidade())
                    .divide(BigDecimal.TEN);

            if (desconto.compareTo(totalCalculado) > 0) {
                desconto = totalCalculado;
            }

            totalCalculado = totalCalculado.subtract(desconto);

            int pontosUtilizados = desconto.multiply(BigDecimal.TEN).intValue();

            cliente.setPontosFidelidade(
                    cliente.getPontosFidelidade() - pontosUtilizados
            );

            clienteRepository.save(cliente);
        }

        pedido.setValorTotal(totalCalculado);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        produtoPedidoRepository.saveAll(itensPedido);

        boolean pagamentoAprovado = pagamentoClient.processarPagamento(pedidoSalvo.getId(), request.formaPagamento());

        if (pagamentoAprovado) {
            pedidoSalvo.setStatus(StatusPedido.PREPARANDO);
        } else {
            rollbackEstoque(pedidoSalvo);
            pedidoSalvo.setStatus(StatusPedido.PAGAMENTO_RECUSADO);
        }

        pedidoRepository.save(pedidoSalvo);

        return new PedidoResponseDTO(
                pedidoSalvo.getId(),
                pedidoSalvo.getStatus().name(),
                pedidoSalvo.getValorTotal()
        );
    }

    @Transactional
    public PedidoResponseDTO processarNovoPagamento(Long pedidoId, String formaPagamento) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));

        if (pedido.getStatus() != StatusPedido.PAGAMENTO_RECUSADO) {
            throw new RegraNegocioException("Este pedido não permite nova tentativa de pagamento.");
        }

        if (pedido.getDataCriacao().isBefore(OffsetDateTime.now().minusMinutes(30))) {
            pedido.setStatus(StatusPedido.CANCELADO);
            pedidoRepository.save(pedido);
            throw new RegraNegocioException("Pedido expirado. Por favor, crie um novo.");
        }

        List<ProdutoPedido> itens = pedido.getItens();
        for (ProdutoPedido item : itens) {
            ProdutoUnidade estoque = estoqueRepository.findByUnidadeIdAndProdutoId(
                            pedido.getUnidade().getId(), item.getProduto().getId())
                    .orElseThrow(() -> new RegraNegocioException("Produto indisponível"));

            verificarDisponibilidade(estoque, item.getQuantidade());

            estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() - item.getQuantidade());
            estoqueRepository.save(estoque);
        }

        boolean pagamentoAprovado = pagamentoClient.processarPagamento(pedido.getId(), formaPagamento);

        if (pagamentoAprovado) {
            pedido.setStatus(StatusPedido.PREPARANDO);
        } else {
            rollbackEstoque(pedido);
            pedido.setStatus(StatusPedido.PAGAMENTO_RECUSADO);
        }

        pedidoRepository.save(pedido);

        return new PedidoResponseDTO(pedido.getId(), pedido.getStatus().name(), pedido.getValorTotal());
    }

    public void verificarDisponibilidade(ProdutoUnidade estoque, Integer quantidadeDesejada) {
        if (!estoque.getDisponivelCardapio()) {
            throw new RegraNegocioException("Produto indisponível no cardápio desta unidade.");
        }

        if (estoque.getQuantidadeEstoque() < quantidadeDesejada) {
            throw new RegraNegocioException("Quantidade insuficiente em estoque. Disponível: " + estoque.getQuantidadeEstoque());
        }
    }

    private void rollbackEstoque(Pedido pedido) {
        for (ProdutoPedido item : pedido.getItens()) {
            ProdutoUnidade estoque = estoqueRepository.findByUnidadeIdAndProdutoId(
                    pedido.getUnidade().getId(), item.getProduto().getId())
                        .orElseThrow(() -> new NotFoundException("Produto não disponível nesta unidade"));
            estoque.setQuantidadeEstoque(estoque.getQuantidadeEstoque() + item.getQuantidade());
            estoqueRepository.save(estoque);
        }
    }

    @Transactional
    public PedidoResponseDTO atualizarStatus(Long pedidoId, AtualizarStatusRequestDTO request) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));

        if (request.status() == StatusPedido.CANCELADO &&
                (pedido.getStatus() == StatusPedido.PREPARANDO || pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO)) {
            rollbackEstoque(pedido);
        }

        if (request.status() == StatusPedido.ENTREGUE
                && pedido.getStatus() != StatusPedido.ENTREGUE
                && pedido.getCliente() != null
                && pedido.getCliente().getAceitaProgramaFidelidade()) {

            Cliente cliente = pedido.getCliente();

            cliente.setPontosFidelidade(
                    cliente.getPontosFidelidade()
                            + pedido.getValorTotal().intValue()
            );

            clienteRepository.save(cliente);
        }

        pedido.setStatus(request.status());
        pedidoRepository.save(pedido);

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getStatus().name(),
                pedido.getValorTotal()
        );
    }
}