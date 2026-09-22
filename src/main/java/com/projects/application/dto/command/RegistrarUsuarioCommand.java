package com.projects.application.dto.command;

public record RegistrarUsuarioCommand(
        String email,
        String telefono,
        String password
) {
}
