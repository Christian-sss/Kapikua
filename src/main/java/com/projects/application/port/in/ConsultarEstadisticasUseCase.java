package com.projects.application.port.in;

import com.projects.application.dto.command.ConsultarEstadisticasCommand;
import com.projects.application.dto.response.EstadisticasResponse;
import com.projects.domain.result.Result;

public interface ConsultarEstadisticasUseCase {

    Result<EstadisticasResponse> ejecutar(ConsultarEstadisticasCommand command);

}
