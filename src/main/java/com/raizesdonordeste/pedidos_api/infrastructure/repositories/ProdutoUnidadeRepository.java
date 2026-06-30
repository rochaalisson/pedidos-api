package com.raizesdonordeste.pedidos_api.infrastructure.repositories;

import com.raizesdonordeste.pedidos_api.domain.models.ProdutoUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoUnidadeRepository extends JpaRepository<ProdutoUnidade, Long> {
    Optional<ProdutoUnidade> findByUnidadeIdAndProdutoId(Long unidadeId, Long produtoId);
    List<ProdutoUnidade> findByUnidadeIdAndDisponivelCardapioTrue(Long unidadeId);
}
