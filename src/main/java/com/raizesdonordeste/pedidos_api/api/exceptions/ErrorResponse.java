package com.raizesdonordeste.pedidos_api.api.exceptions;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        String error,
        String message,
        List<String> details,
        OffsetDateTime timestamp,
        String path
) {}