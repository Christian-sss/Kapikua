package com.projects.application.port.in;

import com.projects.application.dto.command.ConsultarMovimientosCommand;
import com.projects.application.dto.response.MovimientoHistorialResponse;
import com.projects.domain.result.Result;

import java.util.List;

public interface ConsultarMovimientosUseCase {

    Result<List<MovimientoHistorialResponse>> ejecutar(ConsultarMovimientosCommand command);

}
