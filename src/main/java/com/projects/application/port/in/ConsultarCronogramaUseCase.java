package com.projects.application.port.in;

import com.projects.application.dto.command.ConsultarCronogramaCommand;
import com.projects.application.dto.response.CuotaCronogramaResponse;
import com.projects.domain.result.Result;

import java.util.List;

public interface ConsultarCronogramaUseCase {

    Result<List<CuotaCronogramaResponse>> ejecutar(ConsultarCronogramaCommand command);

}
