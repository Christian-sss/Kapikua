package com.projects.application.dto.response;

public record ClienteRegistradoResponse(
        Long clienteId,
        String email,
        String messsage
) {
}
