package com.projects.application.dto.command;

public record PagarCuotaCommand(
        Long clienteId,
        Long prestamoId
) {
}
