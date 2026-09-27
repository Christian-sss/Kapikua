package com.projects.application.port.out;

import com.projects.domain.model.billetera.Movimiento;

import java.util.Optional;

public interface MovimientoRepository {

    Optional<Movimiento> guardar(Movimiento movimiento);

}
