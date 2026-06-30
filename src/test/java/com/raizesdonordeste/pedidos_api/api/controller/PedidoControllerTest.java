package com.raizesdonordeste.pedidos_api.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raizesdonordeste.pedidos_api.api.dto.PedidoRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.PedidoResponseDTO;
import com.raizesdonordeste.pedidos_api.api.exceptions.NotFoundException;
import com.raizesdonordeste.pedidos_api.api.exceptions.RegraNegocioException;
import com.raizesdonordeste.pedidos_api.application.services.PedidoService;
import com.raizesdonordeste.pedidos_api.domain.enums.CanalPedido;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
@Import({SecurityConfig.class, PedidoControllerTest.Config.class})
class PedidoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean PedidoService pedidoService;
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
    @WithMockUser(roles = "GERENTE")
    void listarPedidos_autenticadoComoGerente_retorna200() throws Exception {
        when(pedidoService.listar(any())).thenReturn(List.of());

        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void listarPedidos_comFiltroCanal_retorna200() throws Exception {
        when(pedidoService.listar(CanalPedido.TOTEM)).thenReturn(List.of());

        mockMvc.perform(get("/pedidos").param("canalPedido", "TOTEM"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPedidos_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/pedidos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void criarPedido_dadosValidos_retorna201() throws Exception {
        var request = new PedidoRequestDTO(1L, null, CanalPedido.APP, "CARTAO_CREDITO", false,
                List.of(new PedidoRequestDTO.ItemRequestDTO(1L, 2)));

        when(pedidoService.criarPedido(any()))
                .thenReturn(new PedidoResponseDTO(1L, "PREPARANDO", new BigDecimal("20.00")));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pedidoId").value(1))
                .andExpect(jsonPath("$.status").value("PREPARANDO"))
                .andExpect(jsonPath("$.total").value(20.00));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void criarPedido_produtoNaoEncontrado_retorna404() throws Exception {
        var request = new PedidoRequestDTO(1L, null, CanalPedido.APP, "CARTAO_CREDITO", false,
                List.of(new PedidoRequestDTO.ItemRequestDTO(999L, 1)));

        when(pedidoService.criarPedido(any()))
                .thenThrow(new NotFoundException("Produto não encontrado: 999"));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NAO_ENCONTRADO"));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void criarPedido_estoqueInsuficiente_retorna422() throws Exception {
        var request = new PedidoRequestDTO(1L, null, CanalPedido.APP, "CARTAO_CREDITO", false,
                List.of(new PedidoRequestDTO.ItemRequestDTO(1L, 100)));

        when(pedidoService.criarPedido(any()))
                .thenThrow(new RegraNegocioException("Quantidade insuficiente em estoque. Disponível: 5"));

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("ERRO_NEGOCIO"));
    }
}
