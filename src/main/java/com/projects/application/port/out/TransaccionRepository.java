package com.projects.application.port.out;

import com.projects.domain.model.billetera.Transaccion;

import java.util.Optional;

public interface TransaccionRepository {

    Optional<Transaccion> guardar(Transaccion transaccion);

}
