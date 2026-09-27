package com.projects.application.dto.response;

import java.math.BigDecimal;

public record VolumenTipoResponse(
        String tipo,
        long cantidad,
        BigDecimal monto
) {
}
