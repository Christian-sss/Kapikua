package com.projects.domain.result;

public enum CreditoError {

    PRODUCTO_NO_ENCONTRADO,
    FUERA_DE_RANGO,
    PRESTAMO_NO_ENCONTRADO,
    SIN_PERMISO,
    PRESTAMO_NO_VIGENTE,
    SIN_CUOTAS_PENDIENTES,

    // Motivos de rechazo: se guardan tal cual en credito.solicitud_credito.motivo_rechazo.
    PRESTAMO_EN_MORA,
    LIMITE_PRESTAMOS,
    REQUIERE_MICROCREDITO_PAGADO

}
