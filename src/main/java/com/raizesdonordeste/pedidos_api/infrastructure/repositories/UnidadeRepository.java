package com.raizesdonordeste.pedidos_api.infrastructure.repositories;

import com.raizesdonordeste.pedidos_api.domain.models.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
}
