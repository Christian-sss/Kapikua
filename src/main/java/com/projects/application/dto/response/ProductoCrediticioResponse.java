package com.projects.application.dto.response;

import java.math.BigDecimal;

public record ProductoCrediticioResponse(
        Long id,
        String nombre,
        String tipo,
        BigDecimal montoMinimo,
        BigDecimal montoMaximo,
        Integer plazoMinimoMeses,
        Integer plazoMaximoMeses,
        BigDecimal tasaInteresAnual
) {
}
