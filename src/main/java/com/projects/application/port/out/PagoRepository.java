package com.projects.application.port.out;

import com.projects.domain.model.credito.Pago;
import com.projects.domain.model.credito.PagoDetalle;

import java.util.Optional;

public interface PagoRepository {

    Optional<Pago> guardar(Pago pago);

    void guardarDetalle(PagoDetalle detalle);

}
