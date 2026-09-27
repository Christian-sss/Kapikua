package com.projects.application.service;

import com.projects.application.dto.command.ConsultarPrestamosCommand;
import com.projects.application.dto.response.PrestamoResumenResponse;
import com.projects.application.port.in.ConsultarPrestamosUseCase;
import com.projects.application.port.out.PrestamoQuery;
import com.projects.domain.result.Result;

import java.util.List;

public class ConsultarPrestamosService implements ConsultarPrestamosUseCase {

    private final PrestamoQuery prestamoQuery;

    public ConsultarPrestamosService(PrestamoQuery prestamoQuery) {
        this.prestamoQuery = prestamoQuery;
    }

    @Override
    public Result<List<PrestamoResumenResponse>> ejecutar(ConsultarPrestamosCommand command) {

        if (command == null || command.clienteId() == null) {
            return Result.failure("COMANDO_INVALIDO", "El cliente es obligatorio.");
        }

        return Result.success(prestamoQuery.buscarPorClienteId(command.clienteId()));
    }
}
