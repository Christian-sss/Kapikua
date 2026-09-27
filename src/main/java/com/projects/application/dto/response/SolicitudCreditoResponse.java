package com.projects.application.dto.response;

import java.math.BigDecimal;

/**
 * Una solicitud rechazada también es un resultado exitoso del caso de uso (se guarda con su motivo),
 * por eso viaja en un Success y no en un Failure. Los campos del préstamo son null si fue rechazada.
 */
public record SolicitudCreditoResponse(
        Long solicitudId,
        String estado,
        String motivoRechazo,
        String mensaje,
        Long prestamoId,
        Long transaccionDesembolsoId,
        BigDecimal montoDesembolsado,
        BigDecimal cuotaMensual,
        BigDecimal saldoResultante
) {

    public boolean aprobada() {
        return prestamoId != null;
    }
}
