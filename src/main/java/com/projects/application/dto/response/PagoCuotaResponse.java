package com.projects.application.dto.response;

import java.math.BigDecimal;

public record PagoCuotaResponse(
        Long transaccionId,
        Long prestamoId,
        Integer numeroCuota,
        BigDecimal montoPagado,
        BigDecimal moraAplicada,
        BigDecimal interesAplicado,
        BigDecimal capitalAplicado,
        BigDecimal saldoResultante,
        BigDecimal saldoCapitalPrestamo,
        String estadoPrestamo
) {

    public boolean prestamoPagado() {
        return "PAGADO".equals(estadoPrestamo);
    }
}
