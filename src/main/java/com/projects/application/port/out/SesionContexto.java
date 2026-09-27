package com.projects.application.port.out;

import com.projects.application.dto.response.SesionIniciadaResponse;

import java.util.Optional;

public interface SesionContexto {

    void iniciar(SesionIniciadaResponse sesion);

    void cerrar();

    Optional<SesionIniciadaResponse> obtenerActual();

}
