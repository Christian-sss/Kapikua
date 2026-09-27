package com.projects.application.port.out;

import com.projects.domain.model.credito.Prestamo;

import java.util.Optional;

public interface PrestamoRepository {

    Optional<Prestamo> guardar(Prestamo prestamo);

    /**
     * Bloquea la fila (SELECT ... FOR UPDATE): usar dentro de una transacción antes de cambiar
     * el saldo de capital o el estado.
     */
    Optional<Prestamo> findByIdParaActualizar(Long id);

    void actualizar(Prestamo prestamo);

}
