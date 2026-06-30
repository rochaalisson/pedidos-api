package com.raizesdonordeste.pedidos_api.application.services;

import com.raizesdonordeste.pedidos_api.api.dto.AtualizarStatusRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.PedidoRequestDTO;
import com.raizesdonordeste.pedidos_api.api.exceptions.NotFoundException;
import com.raizesdonordeste.pedidos_api.api.exceptions.RegraNegocioException;
import com.raizesdonordeste.pedidos_api.domain.enums.CanalPedido;
import com.raizesdonordeste.pedidos_api.domain.enums.StatusPedido;
import com.raizesdonordeste.pedidos_api.domain.models.*;
import com.raizesdonordeste.pedidos_api.infrastructure.external.PagamentoMockClient;
import com.raizesdonordeste.pedidos_api.infrastructure.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock PedidoRepository pedidoRepository;
    @Mock ProdutoRepository produtoRepository;
    @Mock ProdutoUnidadeRepository estoqueRepository;
    @Mock ProdutoPedidoRepository produtoPedidoRepository;
    @Mock UnidadeRepository unidadeRepository;
    @Mock ClienteRepository clienteRepository;
    @Mock PagamentoMockClient pagamentoClient;

    @InjectMocks PedidoService pedidoService;

    @Test
    void criarPedido_pagamentoAprovado_retornaStatusPreparando() {
        var request = new PedidoRequestDTO(1L, null, CanalPedido.APP, "CARTAO_CREDITO", false,
                List.of(new PedidoRequestDTO.ItemRequestDTO(1L, 2)));

        var unidade = Unidade.builder().id(1L).nome("Central").endereco("Rua X").build();
        var produto = Produto.builder().id(1L).nome("Tapioca").preco(new BigDecimal("10.00")).build();
        var estoque = ProdutoUnidade.builder()
                .id(1L).unidade(unidade).produto(produto)
                .quantidadeEstoque(10).disponivelCardapio(true).build();
        var pedidoSalvo = Pedido.builder()
                .id(1L).unidade(unidade).canal(CanalPedido.APP)
                .status(StatusPedido.AGUARDANDO_PAGAMENTO)
                .valorTotal(new BigDecimal("20.00"))
                .itens(new ArrayList<>())
                .dataCriacao(OffsetDateTime.now()).build();

        when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.of(estoque));
        when(pedidoRepository.save(any())).thenReturn(pedidoSalvo);
        when(produtoPedidoRepository.saveAll(any())).thenReturn(List.of());
        when(pagamentoClient.processarPagamento(anyLong(), anyString())).thenReturn(true);

        var response = pedidoService.criarPedido(request);

        assertEquals("PREPARANDO", response.status());
        assertEquals(1L, response.pedidoId());
    }

    @Test
    void criarPedido_pagamentoRecusado_retornaStatusPagamentoRecusado() {
        var request = new PedidoRequestDTO(1L, null, CanalPedido.APP, "CARTAO_CREDITO", false,
                List.of(new PedidoRequestDTO.ItemRequestDTO(1L, 2)));

        var unidade = Unidade.builder().id(1L).nome("Central").endereco("Rua X").build();
        var produto = Produto.builder().id(1L).nome("Tapioca").preco(new BigDecimal("10.00")).build();
        var estoque = ProdutoUnidade.builder()
                .id(1L).unidade(unidade).produto(produto)
                .quantidadeEstoque(10).disponivelCardapio(true).build();
        var itemPedido = ProdutoPedido.builder()
                .produto(produto).quantidade(2).precoUnitario(new BigDecimal("10.00")).build();
        var pedidoSalvo = Pedido.builder()
                .id(1L).unidade(unidade).canal(CanalPedido.APP)
                .status(StatusPedido.AGUARDANDO_PAGAMENTO)
                .valorTotal(new BigDecimal("20.00"))
                .itens(new ArrayList<>(List.of(itemPedido)))
                .dataCriacao(OffsetDateTime.now()).build();

        when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.of(estoque));
        when(pedidoRepository.save(any())).thenReturn(pedidoSalvo);
        when(produtoPedidoRepository.saveAll(any())).thenReturn(List.of());
        when(pagamentoClient.processarPagamento(anyLong(), anyString())).thenReturn(false);

        var response = pedidoService.criarPedido(request);

        assertEquals("PAGAMENTO_RECUSADO", response.status());
        verify(estoqueRepository, times(2)).save(any());
    }

    @Test
    void criarPedido_unidadeNaoEncontrada_lancaNotFoundException() {
        var request = new PedidoRequestDTO(999L, null, CanalPedido.APP, "CARTAO_CREDITO", false,
                List.of(new PedidoRequestDTO.ItemRequestDTO(1L, 1)));

        when(unidadeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> pedidoService.criarPedido(request));
    }

    @Test
    void criarPedido_estoqueInsuficiente_lancaRegraNegocioException() {
        var request = new PedidoRequestDTO(1L, null, CanalPedido.APP, "CARTAO_CREDITO", false,
                List.of(new PedidoRequestDTO.ItemRequestDTO(1L, 10)));

        var unidade = Unidade.builder().id(1L).nome("Central").endereco("Rua X").build();
        var produto = Produto.builder().id(1L).nome("Tapioca").preco(BigDecimal.TEN).build();
        var estoque = ProdutoUnidade.builder()
                .id(1L).unidade(unidade).produto(produto)
                .quantidadeEstoque(3).disponivelCardapio(true).build();

        when(unidadeRepository.findById(1L)).thenReturn(Optional.of(unidade));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.of(estoque));

        assertThrows(RegraNegocioException.class, () -> pedidoService.criarPedido(request));
    }

    @Test
    void atualizarStatus_paraEntregue_clienteAcumulaPontos() {
        var cliente = Cliente.builder()
                .id(1L).nome("Maria").pontosFidelidade(10)
                .aceitaProgramaFidelidade(true).build();
        var pedido = Pedido.builder()
                .id(1L).status(StatusPedido.PREPARANDO)
                .valorTotal(new BigDecimal("25.00"))
                .cliente(cliente).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenReturn(pedido);

        pedidoService.atualizarStatus(1L, new AtualizarStatusRequestDTO(StatusPedido.ENTREGUE));

        assertEquals(35, cliente.getPontosFidelidade());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void atualizarStatus_paraCancelado_restauraEstoque() {
        var unidade = Unidade.builder().id(1L).nome("Central").build();
        var produto = Produto.builder().id(1L).nome("Tapioca").build();
        var estoque = ProdutoUnidade.builder()
                .id(1L).unidade(unidade).produto(produto).quantidadeEstoque(5).build();
        var item = ProdutoPedido.builder().produto(produto).quantidade(3).build();
        var pedido = Pedido.builder()
                .id(1L).unidade(unidade).status(StatusPedido.PREPARANDO)
                .valorTotal(BigDecimal.TEN).itens(List.of(item)).build();

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.of(estoque));
        when(pedidoRepository.save(any())).thenReturn(pedido);

        pedidoService.atualizarStatus(1L, new AtualizarStatusRequestDTO(StatusPedido.CANCELADO));

        assertEquals(8, estoque.getQuantidadeEstoque());
        verify(estoqueRepository).save(estoque);
    }
}
