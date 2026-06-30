package com.raizesdonordeste.pedidos_api.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_unidade_id", nullable = false)
    private ProdutoUnidade produtoUnidade;

    @Column(nullable = false)
    private Integer quantidadeAlterada;

    @Column(nullable = false, length = 50)
    private String motivo;

    @Column(nullable = false)
    private OffsetDateTime dataMovimentacao;
}