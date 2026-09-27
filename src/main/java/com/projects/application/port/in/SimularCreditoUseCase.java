package com.projects.application.port.in;

import com.projects.application.dto.command.SimularCreditoCommand;
import com.projects.application.dto.response.SimulacionCreditoResponse;
import com.projects.domain.result.Result;

public interface SimularCreditoUseCase {

    Result<SimulacionCreditoResponse> ejecutar(SimularCreditoCommand command);

}
