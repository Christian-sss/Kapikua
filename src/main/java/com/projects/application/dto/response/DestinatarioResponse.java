package com.projects.application.dto.response;

public record DestinatarioResponse(
        Long clienteId,
        String celular,
        String nombreEnmascarado
) {
}
