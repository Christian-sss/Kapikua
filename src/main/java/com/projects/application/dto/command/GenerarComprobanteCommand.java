package com.projects.application.dto.command;

public record GenerarComprobanteCommand(
        Long transaccionId,
        Long clienteSolicitanteId
) {
}
