package com.projects.application.service;

import com.projects.application.dto.command.BuscarDestinatarioCommand;
import com.projects.application.dto.response.DestinatarioResponse;
import com.projects.application.port.in.BuscarDestinatarioUseCase;
import com.projects.application.port.out.ClienteRepository;
import com.projects.domain.result.ClienteError;
import com.projects.domain.result.Result;

public class BuscarDestinatarioService implements BuscarDestinatarioUseCase {

    private final ClienteRepository clienteRepository;

    public BuscarDestinatarioService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Result<DestinatarioResponse> ejecutar(BuscarDestinatarioCommand command) {

        if (command == null || command.clienteSolicitanteId() == null
                || command.celular() == null || command.celular().isBlank()) {
            return Result.failure("COMANDO_INVALIDO", "El celular del destinatario es obligatorio.");
        }

        var celular = command.celular().trim();

        var clienteEncontrado = clienteRepository.findByCelular(celular);

        if (clienteEncontrado.isEmpty()) {
            return Result.failure(ClienteError.DESTINATARIO_NO_ENCONTRADO.name(),
                    "No existe un cliente KAPIKUA con ese número de celular.");
        }

        var cliente = clienteEncontrado.get();

        if (cliente.getId().equals(command.clienteSolicitanteId())) {
            return Result.failure(ClienteError.DESTINATARIO_INVALIDO.name(),
                    "No puedes transferirte dinero a ti mismo.");
        }

        var primerNombre = cliente.getNombres().trim().split("\\s+")[0];
        var inicialApellido = cliente.getApellidos().trim().charAt(0);
        var nombreEnmascarado = primerNombre + " " + inicialApellido + "***";

        return Result.success(new DestinatarioResponse(
                cliente.getId(),
                cliente.getNumeroCelular(),
                nombreEnmascarado
        ));
    }
}
