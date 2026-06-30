package com.raizesdonordeste.pedidos_api.infrastructure.external;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class PagamentoMockClient {

    private final Random random = new Random();

    public boolean processarPagamento(Long pedidoId, String formaPagamento) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return random.nextDouble() > 0.2;
    }
}