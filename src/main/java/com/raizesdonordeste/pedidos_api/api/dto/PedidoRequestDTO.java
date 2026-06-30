package com.raizesdonordeste.pedidos_api.api.dto;

import com.raizesdonordeste.pedidos_api.domain.enums.CanalPedido;

import java.util.List;

public record PedidoRequestDTO(
        Long unidadeId,
        Long clienteId,
        CanalPedido canal,
        String formaPagamento,
        Boolean usarPontos,
        List<ItemRequestDTO> itens
) {
    public record ItemRequestDTO(
            Long produtoId,
            Integer quantidade
    ) {}
}

