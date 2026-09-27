package com.projects.application.dto.command;

public record IniciarSesionCommand(
        String email,
        String password
) {
}
