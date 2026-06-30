package com.raizesdonordeste.pedidos_api.api.dto;

import java.util.List;

public record CategoriaCardapioDTO(
        String nomeCategoria,
        List<ProdutoCardapioDTO> produtos
) {}
