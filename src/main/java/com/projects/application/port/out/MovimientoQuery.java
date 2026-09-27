package com.projects.application.port.out;

import com.projects.application.dto.response.MovimientoHistorialResponse;

import java.util.List;

/**
 * Puerto de solo lectura para el historial: cruza movimiento + transaccion + tipo_transaccion.
 * No es un repositorio de escritura (ese es MovimientoRepository).
 */
public interface MovimientoQuery {

    List<MovimientoHistorialResponse> buscarPorBilleteraId(Long billeteraId);

}
