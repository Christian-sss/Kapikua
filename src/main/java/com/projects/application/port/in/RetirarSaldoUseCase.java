package com.projects.application.port.in;

import com.projects.application.dto.command.RetirarSaldoCommand;
import com.projects.application.dto.response.RetiroResponse;
import com.projects.domain.result.Result;

public interface RetirarSaldoUseCase {

    Result<RetiroResponse> ejecutar(RetirarSaldoCommand command);

}
