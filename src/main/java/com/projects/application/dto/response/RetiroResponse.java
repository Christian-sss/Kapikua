package com.projects.application.dto.response;

import java.math.BigDecimal;

public record RetiroResponse(
        Long transaccionId,
        BigDecimal monto,
        BigDecimal saldoResultante,
        String referencia
) {
}
