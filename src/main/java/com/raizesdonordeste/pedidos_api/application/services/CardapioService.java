package com.raizesdonordeste.pedidos_api.application.services;

import com.raizesdonordeste.pedidos_api.api.dto.CategoriaCardapioDTO;
import com.raizesdonordeste.pedidos_api.api.dto.ProdutoCardapioDTO;
import com.raizesdonordeste.pedidos_api.api.exceptions.NotFoundException;
import com.raizesdonordeste.pedidos_api.domain.models.ProdutoUnidade;
import com.raizesdonordeste.pedidos_api.infrastructure.repositories.ProdutoUnidadeRepository;
import com.raizesdonordeste.pedidos_api.infrastructure.repositories.UnidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final ProdutoUnidadeRepository estoqueRepository;
    private final UnidadeRepository unidadeRepository;

    public List<CategoriaCardapioDTO> consultarCardapioPorUnidade(Long unidadeId) {
        if (!unidadeRepository.existsById(unidadeId)) {
            throw new NotFoundException("Unidade não encontrada");
        }

        List<ProdutoUnidade> produtosDisponiveis = estoqueRepository
                .findByUnidadeIdAndDisponivelCardapioTrue(unidadeId);

        return produtosDisponiveis.stream()
                .collect(Collectors.groupingBy(
                        pu -> pu.getProduto().getCategoria().getNome()
                ))
                .entrySet().stream()
                .map(entry -> {
                    String nomeCategoria = entry.getKey();
                    List<ProdutoCardapioDTO> produtosDto = entry.getValue().stream()
                            .map(pu -> new ProdutoCardapioDTO(
                                    pu.getProduto().getId(),
                                    pu.getProduto().getNome(),
                                    pu.getProduto().getDescricao(),
                                    pu.getProduto().getPreco()
                            ))
                            .toList();
                    return new CategoriaCardapioDTO(nomeCategoria, produtosDto);
                })
                .toList();
    }
}