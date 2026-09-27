package com.projects.application.dto.command;

public record BuscarDestinatarioCommand(
        Long clienteSolicitanteId,
        String celular
) {
}
