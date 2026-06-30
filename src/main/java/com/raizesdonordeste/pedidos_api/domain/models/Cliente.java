package com.raizesdonordeste.pedidos_api.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(unique = true)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String senha;

    @Column(unique = true, length = 11)
    private String cpf;

    @Column(length = 15)
    private String telefone;

    @Column(name = "pontos_fidelidade", nullable = false)
    private Integer pontosFidelidade;

    @Column(name = "aceita_programa_fidelidade", nullable = false)
    private Boolean aceitaProgramaFidelidade;
}