package com.raizesdonordeste.pedidos_api.api.dto;

public record ProdutoUnidadeResponseDTO(
        Long id,
        Long produtoId,
        Long unidadeId,
        Integer quantidadeEstoque,
        Boolean disponivelCardapio
) {
}