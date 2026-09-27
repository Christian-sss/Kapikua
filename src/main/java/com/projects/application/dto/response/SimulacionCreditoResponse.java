package com.projects.application.dto.response;

import com.projects.application.service.support.Cronograma;

import java.math.BigDecimal;

public record SimulacionCreditoResponse(
        Long productoId,
        String productoNombre,
        BigDecimal monto,
        Integer plazoMeses,
        Cronograma cronograma
) {
}
