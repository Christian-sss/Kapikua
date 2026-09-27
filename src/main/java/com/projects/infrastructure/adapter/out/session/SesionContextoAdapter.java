package com.projects.infrastructure.adapter.out.session;

import com.projects.application.dto.response.SesionIniciadaResponse;
import com.projects.application.port.out.SesionContexto;

import java.util.Optional;

public class SesionContextoAdapter implements SesionContexto {

    // No es un ThreadLocal a propósito: el login corre en el hilo de fondo de un
    // SwingWorker, pero cualquier otra pantalla (en el EDT) debe poder leer la
    // sesión activa. volatile asegura que ese cambio de hilo vea el valor actual.
    private static volatile SesionIniciadaResponse sesionActual;

    @Override
    public void iniciar(SesionIniciadaResponse sesion) {
        sesionActual = sesion;
    }

    @Override
    public void cerrar() {
        sesionActual = null;
    }

    @Override
    public Optional<SesionIniciadaResponse> obtenerActual() {
        return Optional.ofNullable(sesionActual);
    }
}
