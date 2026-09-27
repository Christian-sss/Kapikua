package com.projects.application.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MovimientoHistorialResponse(
        Long transaccionId,
        OffsetDateTime fecha,
        String tipoCodigo,
        Character signo,
        BigDecimal monto,
        BigDecimal saldoPosterior,
        String estadoTransaccion
) {
}
