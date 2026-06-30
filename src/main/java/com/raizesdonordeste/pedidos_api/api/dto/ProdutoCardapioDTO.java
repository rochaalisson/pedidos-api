package com.raizesdonordeste.pedidos_api.api.dto;

import java.math.BigDecimal;

public record ProdutoCardapioDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco
) {}