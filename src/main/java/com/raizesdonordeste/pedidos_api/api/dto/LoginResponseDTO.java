package com.raizesdonordeste.pedidos_api.api.dto;

public record LoginResponseDTO(
        String token,
        String tipo,
        String email
) {}
