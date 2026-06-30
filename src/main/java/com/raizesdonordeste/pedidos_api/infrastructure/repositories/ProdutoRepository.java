package com.raizesdonordeste.pedidos_api.infrastructure.repositories;

import com.raizesdonordeste.pedidos_api.domain.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
