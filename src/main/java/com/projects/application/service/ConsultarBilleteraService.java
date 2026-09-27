package com.projects.application.service;

import com.projects.application.dto.command.ConsultarBilleteraCommand;
import com.projects.application.dto.response.BilleteraResponse;
import com.projects.application.port.in.ConsultarBilleteraUseCase;
import com.projects.application.port.out.BilleteraRepository;
import com.projects.domain.result.BilleteraError;
import com.projects.domain.result.Result;

public class ConsultarBilleteraService implements ConsultarBilleteraUseCase {

    private final BilleteraRepository billeteraRepository;

    public ConsultarBilleteraService(BilleteraRepository billeteraRepository) {
        this.billeteraRepository = billeteraRepository;
    }

    @Override
    public Result<BilleteraResponse> ejecutar(ConsultarBilleteraCommand command) {

        if (command == null || command.clienteId() == null) {
            return Result.failure("COMANDO_INVALIDO", "El cliente es obligatorio.");
        }

        return billeteraRepository.findByClienteId(command.clienteId())
                .map(billetera -> Result.success(new BilleteraResponse(
                        billetera.getId(),
                        billetera.getClienteId(),
                        billetera.getSaldo(),
                        billetera.getEstadoBilletera().name()
                )))
                .orElseGet(() -> Result.failure(BilleteraError.BILLETERA_NO_ENCONTRADA.name(),
                        "No se encontró una billetera para este cliente."));
    }
}
