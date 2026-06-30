package com.raizesdonordeste.pedidos_api.api.dto;

import com.raizesdonordeste.pedidos_api.domain.enums.PerfilUsuario;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        Long unidadeId
) {}
