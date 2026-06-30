package com.raizesdonordeste.pedidos_api.application.services;

import com.raizesdonordeste.pedidos_api.api.dto.MovimentacaoEstoqueRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.ProdutoUnidadeResponseDTO;
import com.raizesdonordeste.pedidos_api.api.exceptions.NotFoundException;
import com.raizesdonordeste.pedidos_api.api.exceptions.RegraNegocioException;
import com.raizesdonordeste.pedidos_api.domain.models.MovimentacaoEstoque;
import com.raizesdonordeste.pedidos_api.domain.models.ProdutoUnidade;
import com.raizesdonordeste.pedidos_api.infrastructure.repositories.MovimentacaoEstoqueRepository;
import com.raizesdonordeste.pedidos_api.infrastructure.repositories.ProdutoUnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final ProdutoUnidadeRepository produtoUnidadeRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Transactional
    public ProdutoUnidadeResponseDTO entradaEstoque(Long produtoUnidadeId,
                                                    MovimentacaoEstoqueRequestDTO request) {

        if (request.quantidade() <= 0) {
            throw new RegraNegocioException("Quantidade deve ser maior que zero.");
        }

        ProdutoUnidade produtoUnidade = produtoUnidadeRepository.findById(produtoUnidadeId)
                .orElseThrow(() -> new NotFoundException("Estoque não encontrado."));

        produtoUnidade.setQuantidadeEstoque(
                produtoUnidade.getQuantidadeEstoque() + request.quantidade()
        );

        produtoUnidadeRepository.save(produtoUnidade);

        registrarMovimentacao(
                produtoUnidade,
                request.quantidade(),
                request.motivo()
        );

        return toResponse(produtoUnidade);
    }

    @Transactional
    public ProdutoUnidadeResponseDTO saidaEstoque(Long produtoUnidadeId,
                                                  MovimentacaoEstoqueRequestDTO request) {

        if (request.quantidade() <= 0) {
            throw new RegraNegocioException("Quantidade deve ser maior que zero.");
        }

        ProdutoUnidade produtoUnidade = produtoUnidadeRepository.findById(produtoUnidadeId)
                .orElseThrow(() -> new NotFoundException("Estoque não encontrado."));

        if (produtoUnidade.getQuantidadeEstoque() < request.quantidade()) {
            throw new RegraNegocioException(
                    "Quantidade insuficiente em estoque. Disponível: "
                            + produtoUnidade.getQuantidadeEstoque()
            );
        }

        produtoUnidade.setQuantidadeEstoque(
                produtoUnidade.getQuantidadeEstoque() - request.quantidade()
        );

        produtoUnidadeRepository.save(produtoUnidade);

        registrarMovimentacao(
                produtoUnidade,
                -request.quantidade(),
                request.motivo()
        );

        return toResponse(produtoUnidade);
    }

    public ProdutoUnidadeResponseDTO consultarSaldo(Long produtoUnidadeId) {

        ProdutoUnidade produtoUnidade = produtoUnidadeRepository.findById(produtoUnidadeId)
                .orElseThrow(() -> new NotFoundException("Estoque não encontrado."));

        return toResponse(produtoUnidade);
    }

    private void registrarMovimentacao(ProdutoUnidade produtoUnidade,
                                       Integer quantidade,
                                       String motivo) {

        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.builder()
                .produtoUnidade(produtoUnidade)
                .quantidadeAlterada(quantidade)
                .motivo(motivo)
                .dataMovimentacao(OffsetDateTime.now())
                .build();

        movimentacaoEstoqueRepository.save(movimentacao);
    }

    private ProdutoUnidadeResponseDTO toResponse(ProdutoUnidade produtoUnidade) {
        return new ProdutoUnidadeResponseDTO(
                produtoUnidade.getId(),
                produtoUnidade.getProduto().getId(),
                produtoUnidade.getUnidade().getId(),
                produtoUnidade.getQuantidadeEstoque(),
                produtoUnidade.getDisponivelCardapio()
        );
    }
}