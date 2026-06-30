package com.raizesdonordeste.pedidos_api.api.controller;

import com.raizesdonordeste.pedidos_api.api.dto.MovimentacaoEstoqueRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.ProdutoUnidadeResponseDTO;
import com.raizesdonordeste.pedidos_api.api.exceptions.ErrorResponse;
import com.raizesdonordeste.pedidos_api.application.services.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Controle de estoque por unidade")
@SecurityRequirement(name = "bearerAuth")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @PostMapping("/{produtoUnidadeId}/entrada")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Registrar entrada de estoque", description = "Adiciona unidades ao estoque de um produto em uma unidade. Requer perfil GERENTE ou ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entrada registrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoUnidadeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer GERENTE ou ADMIN)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto/Unidade não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProdutoUnidadeResponseDTO entradaEstoque(
            @PathVariable Long produtoUnidadeId,
            @Valid @RequestBody MovimentacaoEstoqueRequestDTO request) {
        return estoqueService.entradaEstoque(produtoUnidadeId, request);
    }

    @PostMapping("/{produtoUnidadeId}/saida")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @Operation(summary = "Registrar saída manual de estoque", description = "Remove unidades do estoque (perda, descarte, ajuste). Requer perfil GERENTE ou ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saída registrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoUnidadeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão (requer GERENTE ou ADMIN)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto/Unidade não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Estoque insuficiente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProdutoUnidadeResponseDTO saidaEstoque(
            @PathVariable Long produtoUnidadeId,
            @Valid @RequestBody MovimentacaoEstoqueRequestDTO request) {
        return estoqueService.saidaEstoque(produtoUnidadeId, request);
    }

    @GetMapping("/{produtoUnidadeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    @Operation(summary = "Consultar saldo de estoque", description = "Retorna o saldo atual de um produto em uma unidade.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saldo retornado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProdutoUnidadeResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produto/Unidade não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProdutoUnidadeResponseDTO consultarSaldo(@PathVariable Long produtoUnidadeId) {
        return estoqueService.consultarSaldo(produtoUnidadeId);
    }
}
