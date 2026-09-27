package com.projects.application.port.in;

import com.projects.application.dto.command.ConsultarPrestamosCommand;
import com.projects.application.dto.response.PrestamoResumenResponse;
import com.projects.domain.result.Result;

import java.util.List;

public interface ConsultarPrestamosUseCase {

    Result<List<PrestamoResumenResponse>> ejecutar(ConsultarPrestamosCommand command);

}
