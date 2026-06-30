package com.raizesdonordeste.pedidos_api.infrastructure.repositories;

import com.raizesdonordeste.pedidos_api.domain.models.MovimentacaoEstoque;
import com.raizesdonordeste.pedidos_api.domain.models.ProdutoUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
}
