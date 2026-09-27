package com.projects.application.port.in;

import com.projects.application.dto.command.SolicitarCreditoCommand;
import com.projects.application.dto.response.SolicitudCreditoResponse;
import com.projects.domain.result.Result;

public interface SolicitarCreditoUseCase {

    Result<SolicitudCreditoResponse> ejecutar(SolicitarCreditoCommand command);

}
