package com.projects.application.dto.response;

import java.math.BigDecimal;

public record TransferenciaResponse(
        Long transaccionId,
        BigDecimal monto,
        BigDecimal saldoResultante,
        String destinatarioNombreEnmascarado,
        String celularDestino
) {
}
