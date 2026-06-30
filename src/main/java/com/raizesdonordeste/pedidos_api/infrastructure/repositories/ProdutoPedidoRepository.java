package com.raizesdonordeste.pedidos_api.infrastructure.repositories;

import com.raizesdonordeste.pedidos_api.domain.models.ProdutoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoPedidoRepository extends JpaRepository<ProdutoPedido, Long> {
}
