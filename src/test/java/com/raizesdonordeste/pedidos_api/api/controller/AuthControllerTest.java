package com.raizesdonordeste.pedidos_api.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raizesdonordeste.pedidos_api.api.dto.LoginRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.LoginResponseDTO;
import com.raizesdonordeste.pedidos_api.application.services.AuthService;
import com.raizesdonordeste.pedidos_api.infrastructure.security.JwtService;
import com.raizesdonordeste.pedidos_api.infrastructure.security.SecurityConfig;
import com.raizesdonordeste.pedidos_api.infrastructure.security.UsuarioDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthControllerTest.Config.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UsuarioDetailsService usuarioDetailsService;

    @TestConfiguration
    static class Config {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Test
    void login_credenciaisValidas_retorna200ComToken() throws Exception {
        var request = new LoginRequestDTO("admin@raizesdonordeste.com", "admin123");
        var response = new LoginResponseDTO("eyJhbGciOiJIUzI1NiJ9.fake", "Bearer", "admin@raizesdonordeste.com");

        when(authService.autenticar(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiJ9.fake"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.email").value("admin@raizesdonordeste.com"));
    }

    @Test
    void login_semCamposObrigatorios_retorna400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DADOS_INVALIDOS"));
    }
}
