package com.projects.application.port.in;

import com.projects.application.dto.command.IniciarSesionCommand;
import com.projects.application.dto.response.SesionIniciadaResponse;
import com.projects.domain.result.Result;

public interface IniciarSesionUseCase {

    Result<SesionIniciadaResponse> iniciarSesion(IniciarSesionCommand command);

}
