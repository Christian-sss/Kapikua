package com.projects.application.port.in;

import com.projects.application.dto.command.ConsultarEstadisticasCommand;
import com.projects.domain.result.Result;

public interface GenerarReporteEstadisticasUseCase {

    Result<byte[]> ejecutar(ConsultarEstadisticasCommand command);

}
