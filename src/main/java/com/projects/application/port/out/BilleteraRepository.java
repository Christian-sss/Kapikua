package com.projects.application.port.out;

import com.projects.domain.model.billetera.Billetera;

import java.util.Optional;

public interface BilleteraRepository {

    Optional<Billetera> save(Billetera billetera);

    Optional<Billetera> findByClienteId(Long clienteId);

    /**
     * Igual que una búsqueda normal, pero bloquea la fila (SELECT ... FOR UPDATE) para usarse
     * dentro de una transacción antes de debitar/acreditar (evita condiciones de carrera).
     */
    Optional<Billetera> findByIdParaActualizar(Long id);

    void actualizarSaldo(Billetera billetera);

}
