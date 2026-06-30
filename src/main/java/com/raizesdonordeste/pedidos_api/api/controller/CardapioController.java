package com.raizesdonordeste.pedidos_api.api.controller;

import com.raizesdonordeste.pedidos_api.api.dto.CategoriaCardapioDTO;
import com.raizesdonordeste.pedidos_api.api.exceptions.ErrorResponse;
import com.raizesdonordeste.pedidos_api.application.services.CardapioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cardapio")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
@Tag(name = "Cardápio", description = "Consulta de cardápio por unidade")
@SecurityRequirement(name = "bearerAuth")
public class CardapioController {

    private final CardapioService cardapioService;

    @GetMapping("/unidades/{unidadeId}")
    @Operation(summary = "Consultar cardápio da unidade", description = "Retorna os produtos disponíveis agrupados por categoria para a unidade informada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cardápio retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unidade não encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<CategoriaCardapioDTO> consultarCardapio(@PathVariable Long unidadeId) {
        return cardapioService.consultarCardapioPorUnidade(unidadeId);
    }
}
