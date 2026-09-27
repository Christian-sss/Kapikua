package com.projects.application.port.in;

import com.projects.application.dto.command.TransferirMontoCommand;
import com.projects.application.dto.response.TransferenciaResponse;
import com.projects.domain.result.Result;

public interface TransferirMontoUseCase {

    Result<TransferenciaResponse> ejecutar(TransferirMontoCommand command);

}
