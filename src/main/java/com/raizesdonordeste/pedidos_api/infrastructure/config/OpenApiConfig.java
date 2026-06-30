package com.raizesdonordeste.pedidos_api.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Raízes do Nordeste — API de Pedidos",
        version = "1.0",
        description = "API REST para gestão de pedidos da rede de lanchonetes Raízes do Nordeste. Trabalho acadêmico UNINTER — Projeto Multidisciplinar Back-End 2026."
))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {}
