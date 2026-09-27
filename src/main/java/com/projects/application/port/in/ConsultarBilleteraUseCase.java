package com.projects.application.port.in;

import com.projects.application.dto.command.ConsultarBilleteraCommand;
import com.projects.application.dto.response.BilleteraResponse;
import com.projects.domain.result.Result;

public interface ConsultarBilleteraUseCase {

    Result<BilleteraResponse> ejecutar(ConsultarBilleteraCommand command);

}
