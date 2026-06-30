package com.raizesdonordeste.pedidos_api.api.dto;

import com.raizesdonordeste.pedidos_api.domain.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequestDTO(
        @NotNull(message = "O status é obrigatório")
        StatusPedido status
) {}