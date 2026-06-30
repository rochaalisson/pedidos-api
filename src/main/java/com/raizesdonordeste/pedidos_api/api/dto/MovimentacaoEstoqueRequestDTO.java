package com.raizesdonordeste.pedidos_api.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MovimentacaoEstoqueRequestDTO(
        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1)
        Integer quantidade,
        String motivo
) {}