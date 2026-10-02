package com.projects.application.service;


import com.projects.application.port.in.CerrarSesionUseCase;
import com.projects.application.port.out.SesionContexto;
import com.projects.application.port.out.SesionRepository;
import com.projects.domain.result.Result;

public class CerrarSesionService implements CerrarSesionUseCase {

    private final SesionContexto sesionContexto;
    private final SesionRepository sesionRepository;

    public CerrarSesionService(SesionContexto sesionContexto, SesionRepository sesionRepository) {
        this.sesionContexto = sesionContexto;
        this.sesionRepository = sesionRepository;
    }

    @Override
    public Result<Void> cerrarSesion() {
        // La sesión local se cierra siempre; si la BD falla, la fila expira sola por falta de latido.
        try {
            sesionContexto.obtenerActual()
                    .filter(s -> s.token() != null)
                    .ifPresent(s -> sesionRepository.eliminar(s.usuarioId(), s.token()));
        } finally {
            sesionContexto.cerrar();
        }
        return Result.success();
    }

}
