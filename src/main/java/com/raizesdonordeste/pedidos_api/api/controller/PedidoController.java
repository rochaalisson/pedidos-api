package com.raizesdonordeste.pedidos_api.api.controller;

import com.raizesdonordeste.pedidos_api.api.dto.AtualizarStatusRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.PedidoListagemResponseDTO;
import com.raizesdonordeste.pedidos_api.api.dto.PedidoRequestDTO;
import com.raizesdonordeste.pedidos_api.api.dto.PedidoResponseDTO;
import com.raizesdonordeste.pedidos_api.api.exceptions.ErrorResponse;
import com.raizesdonordeste.pedidos_api.application.services.PedidoService;
import com.raizesdonordeste.pedidos_api.domain.enums.CanalPedido;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos da rede Raízes do Nordeste")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Retorna todos os pedidos. Use o parâmetro opcional `canalPedido` para filtrar por canal de atendimento (APP, TOTEM, BALCAO, PICKUP, WEB).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<PedidoListagemResponseDTO>> listar(
            @RequestParam(required = false) CanalPedido canalPedido) {
        return ResponseEntity.ok(pedidoService.listar(canalPedido));
    }

    @PostMapping
    @Operation(summary = "Criar um novo pedido", description = "Registra um novo pedido, abate o estoque da unidade solicitada e simula o processamento do pagamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido criado e processado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Unidade ou Produto não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (ex: estoque insuficiente)", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PedidoResponseDTO> criarPedido(@Valid @RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO response = pedidoService.criarPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/pagar")
    @Operation(summary = "Tentar pagamento novamente", description = "Processa uma nova tentativa de pagamento para pedidos com status PAGAMENTO_RECUSADO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nova tentativa de pagamento processada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Pedido expirado, status inválido ou estoque indisponível", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PedidoResponseDTO> processarNovoPagamento(
            @PathVariable Long id,
            @RequestParam String formaPagamento) {
        PedidoResponseDTO response = pedidoService.processarNovoPagamento(id, formaPagamento);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido", description = "Altera o status de preparação ou pagamento de um pedido existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Status não reconhecido pelas regras de negócio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusRequestDTO request) {

        PedidoResponseDTO response = pedidoService.atualizarStatus(id, request);
        return ResponseEntity.ok(response);
    }
}