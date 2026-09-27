package com.projects.application.dto.command;

public record ConsultarCronogramaCommand(
        Long clienteId,
        Long prestamoId
) {
}
