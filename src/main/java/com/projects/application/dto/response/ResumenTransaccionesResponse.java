package com.projects.application.dto.response;

import java.math.BigDecimal;

public record ResumenTransaccionesResponse(
        long total,
        long exitosas,
        BigDecimal volumen
) {
}
