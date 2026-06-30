package com.raizesdonordeste.pedidos_api.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PedidoListagemResponseDTO(
        Long id,
        String status,
        String canal,
        BigDecimal valorTotal,
        OffsetDateTime dataCriacao,
        Long unidadeId,
        Long clienteId,
        Long usuarioId
) {}
