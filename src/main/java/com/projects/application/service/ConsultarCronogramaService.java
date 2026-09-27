package com.projects.application.service;

import com.projects.application.dto.command.ConsultarCronogramaCommand;
import com.projects.application.dto.response.CuotaCronogramaResponse;
import com.projects.application.port.in.ConsultarCronogramaUseCase;
import com.projects.application.port.out.PrestamoQuery;
import com.projects.domain.result.CreditoError;
import com.projects.domain.result.Result;

import java.util.List;

public class ConsultarCronogramaService implements ConsultarCronogramaUseCase {

    private final PrestamoQuery prestamoQuery;

    public ConsultarCronogramaService(PrestamoQuery prestamoQuery) {
        this.prestamoQuery = prestamoQuery;
    }

    @Override
    public Result<List<CuotaCronogramaResponse>> ejecutar(ConsultarCronogramaCommand command) {

        if (command == null || command.clienteId() == null || command.prestamoId() == null) {
            return Result.failure("COMANDO_INVALIDO", "El cliente y el préstamo son obligatorios.");
        }

        var duenio = prestamoQuery.buscarClienteIdDelPrestamo(command.prestamoId());

        if (duenio.isEmpty()) {
            return Result.failure(CreditoError.PRESTAMO_NO_ENCONTRADO.name(), "No se encontró el préstamo.");
        }

        if (!duenio.get().equals(command.clienteId())) {
            return Result.failure(CreditoError.SIN_PERMISO.name(), "No tienes acceso al cronograma de este préstamo.");
        }

        return Result.success(prestamoQuery.buscarCuotas(command.prestamoId()));
    }
}
