package com.projects.application.service;

import com.projects.application.dto.command.ConsultarMovimientosCommand;
import com.projects.application.dto.response.MovimientoHistorialResponse;
import com.projects.application.port.in.ConsultarMovimientosUseCase;
import com.projects.application.port.out.BilleteraRepository;
import com.projects.application.port.out.MovimientoQuery;
import com.projects.domain.result.BilleteraError;
import com.projects.domain.result.Result;

import java.util.List;

public class ConsultarMovimientosService implements ConsultarMovimientosUseCase {

    private final BilleteraRepository billeteraRepository;
    private final MovimientoQuery movimientoQuery;

    public ConsultarMovimientosService(BilleteraRepository billeteraRepository, MovimientoQuery movimientoQuery) {
        this.billeteraRepository = billeteraRepository;
        this.movimientoQuery = movimientoQuery;
    }

    @Override
    public Result<List<MovimientoHistorialResponse>> ejecutar(ConsultarMovimientosCommand command) {

        if (command == null || command.clienteId() == null) {
            return Result.failure("COMANDO_INVALIDO", "El cliente es obligatorio.");
        }

        var billetera = billeteraRepository.findByClienteId(command.clienteId());

        if (billetera.isEmpty()) {
            return Result.failure(BilleteraError.BILLETERA_NO_ENCONTRADA.name(),
                    "No se encontró una billetera para este cliente.");
        }

        return Result.success(movimientoQuery.buscarPorBilleteraId(billetera.get().getId()));
    }
}
