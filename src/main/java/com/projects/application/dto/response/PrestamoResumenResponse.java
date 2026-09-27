package com.projects.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * proximoVencimiento y proximaCuota son null cuando ya no quedan cuotas por pagar.
 */
public record PrestamoResumenResponse(
        Long prestamoId,
        String productoNombre,
        String tipoProducto,
        BigDecimal montoDesembolsado,
        BigDecimal saldoCapital,
        BigDecimal tcea,
        String estado,
        OffsetDateTime fechaDesembolso,
        int cuotasPagadas,
        int cuotasTotales,
        LocalDate proximoVencimiento,
        BigDecimal proximaCuota
) {
}
