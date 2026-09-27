package com.projects.application.port.in;

import com.projects.application.dto.command.GenerarComprobanteCommand;
import com.projects.domain.result.Result;

public interface GenerarComprobanteUseCase {

    Result<byte[]> ejecutar(GenerarComprobanteCommand command);

}
