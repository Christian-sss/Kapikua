package com.projects.application.port.in;

import com.projects.application.dto.command.PagarCuotaCommand;
import com.projects.application.dto.response.PagoCuotaResponse;
import com.projects.domain.result.Result;

public interface PagarCuotaUseCase {

    Result<PagoCuotaResponse> ejecutar(PagarCuotaCommand command);

}
