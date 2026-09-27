package com.projects.application.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ComprobanteResponse(
        Long transaccionId,
        OffsetDateTime fecha,
        String tipoCodigo,
        BigDecimal monto,
        String estado,
        String origenNombre,
        String destinoNombre,
        BigDecimal saldoResultanteSolicitante
) {
}
