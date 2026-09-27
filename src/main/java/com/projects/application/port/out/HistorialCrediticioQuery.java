package com.projects.application.port.out;

import com.projects.domain.model.TipoProductoCrediticio;

/**
 * Puerto de solo lectura con los datos que necesitan los filtros de evaluación de una solicitud.
 */
public interface HistorialCrediticioQuery {

    boolean tienePrestamoEnMora(Long clienteId);

    boolean tienePrestamoActivo(Long clienteId, TipoProductoCrediticio tipo);

    boolean tienePrestamoPagado(Long clienteId, TipoProductoCrediticio tipo);

}
