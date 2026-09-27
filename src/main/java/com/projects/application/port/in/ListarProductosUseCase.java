package com.projects.application.port.in;

import com.projects.application.dto.response.ProductoCrediticioResponse;
import com.projects.domain.result.Result;

import java.util.List;

public interface ListarProductosUseCase {

    Result<List<ProductoCrediticioResponse>> ejecutar();

}
