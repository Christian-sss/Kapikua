package com.projects.application.port.out;

import com.projects.domain.model.credito.Cuota;

import java.util.List;
import java.util.Optional;

public interface CuotaRepository {

    void guardarTodas(List<Cuota> cuotas);

    /**
     * La cuota no pagada de menor número, bloqueada (FOR UPDATE) para pagarla dentro de una transacción.
     */
    Optional<Cuota> buscarPrimeraNoPagadaParaActualizar(Long prestamoId);

    void actualizarEstado(Cuota cuota);

    boolean existeNoPagada(Long prestamoId);

    boolean existeVencida(Long prestamoId);

}
