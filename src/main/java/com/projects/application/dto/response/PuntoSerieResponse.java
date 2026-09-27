package com.projects.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PuntoSerieResponse(
        LocalDate inicio,
        String etiqueta,
        BigDecimal monto
) {
}
