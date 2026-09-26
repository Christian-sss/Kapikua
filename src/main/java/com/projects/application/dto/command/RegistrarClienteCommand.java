package com.projects.application.dto.command;

public record RegistrarClienteCommand(
        String email,
        String password,
        String nombres,
        String apellidos,
        String dni,
        String celular
) {
}
