package com.projects.application.port.out;

import com.projects.application.dto.response.CuotaCronogramaResponse;
import com.projects.application.dto.response.PrestamoResumenResponse;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de solo lectura para "Mis préstamos": cruza prestamo + solicitud_credito + producto + cuota.
 */
public interface PrestamoQuery {

    List<PrestamoResumenResponse> buscarPorClienteId(Long clienteId);

    Optional<Long> buscarClienteIdDelPrestamo(Long prestamoId);

    List<CuotaCronogramaResponse> buscarCuotas(Long prestamoId);

}
