package com.projects.application.port.out;

import com.projects.domain.model.credito.SolicitudCredito;

import java.util.Optional;

public interface SolicitudCreditoRepository {

    Optional<SolicitudCredito> guardar(SolicitudCredito solicitud);

}
