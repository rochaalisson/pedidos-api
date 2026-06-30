package com.raizesdonordeste.pedidos_api.api.controller;

import com.raizesdonordeste.pedidos_api.application.services.EstoqueService;
import com.raizesdonordeste.pedidos_api.infrastructure.security.JwtService;
import com.raizesdonordeste.pedidos_api.infrastructure.security.SecurityConfig;
import com.raizesdonordeste.pedidos_api.infrastructure.security.UsuarioDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstoqueController.class)
@Import(SecurityConfig.class)
class EstoqueControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean EstoqueService estoqueService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UsuarioDetailsService usuarioDetailsService;

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void entradaEstoque_perfilAtendente_retorna403() throws Exception {
        mockMvc.perform(post("/estoque/1/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 10, \"motivo\": \"Reposicao semanal\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void saidaEstoque_perfilGerente_retorna200() throws Exception {
        mockMvc.perform(post("/estoque/1/saida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 5, \"motivo\": \"Descarte\"}"))
                .andExpect(status().isOk());
    }
}
