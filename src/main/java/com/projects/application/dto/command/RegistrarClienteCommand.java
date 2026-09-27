package com.projects.application.dto.command;

import java.time.LocalDate;

public record RegistrarClienteCommand(
        String email,
        String password,
        String nombres,
        String apellidos,
        String dni,
        String celular,
        LocalDate fechaNacimiento
) {
}
