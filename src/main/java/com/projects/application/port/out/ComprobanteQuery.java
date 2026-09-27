package com.projects.application.port.out;

import com.projects.application.dto.response.ComprobanteFilaResponse;

import java.util.List;

public interface ComprobanteQuery {

    List<ComprobanteFilaResponse> buscarPorTransaccionId(Long transaccionId);

}
