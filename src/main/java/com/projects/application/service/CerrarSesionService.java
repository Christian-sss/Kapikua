package com.projects.application.service;


import com.projects.application.port.in.CerrarSesionUseCase;
import com.projects.application.port.out.SesionContexto;
import com.projects.domain.result.Result;

public class CerrarSesionService implements CerrarSesionUseCase {

    private final SesionContexto sesionContexto;

    public CerrarSesionService(SesionContexto sesionContexto) {
        this.sesionContexto = sesionContexto;
    }

    @Override
    public Result<Void> ejecutar() {
        sesionContexto.cerrar();
        return Result.success();
    }

}
