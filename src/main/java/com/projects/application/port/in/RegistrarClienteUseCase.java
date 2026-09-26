package com.projects.application.port.in;

import com.projects.application.dto.command.RegistrarClienteCommand;
import com.projects.application.dto.response.ClienteRegistradoResponse;
import com.projects.domain.result.Result;

public interface RegistrarClienteUseCase {

    Result<ClienteRegistradoResponse> ejecutar(
            RegistrarClienteCommand command
    );


}
