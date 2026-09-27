package com.projects.application.port.in;

import com.projects.application.dto.command.BuscarDestinatarioCommand;
import com.projects.application.dto.response.DestinatarioResponse;
import com.projects.domain.result.Result;

public interface BuscarDestinatarioUseCase {

    Result<DestinatarioResponse> ejecutar(BuscarDestinatarioCommand command);

}
