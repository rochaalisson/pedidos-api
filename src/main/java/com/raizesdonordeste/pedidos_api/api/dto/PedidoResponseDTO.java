package com.raizesdonordeste.pedidos_api.api.dto;

import java.math.BigDecimal;

public record PedidoResponseDTO(
        Long pedidoId,
        String status,
        BigDecimal total
) {}