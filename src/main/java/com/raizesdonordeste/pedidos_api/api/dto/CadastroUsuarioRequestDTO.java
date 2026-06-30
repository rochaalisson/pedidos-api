package com.raizesdonordeste.pedidos_api.api.dto;

import com.raizesdonordeste.pedidos_api.domain.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequestDTO(
        @NotBlank
        String nome,
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 6)
        String senha,
        @NotNull
        PerfilUsuario perfil,
        Long unidadeId
) {}
