package com.projects.application.dto.response;

public record SesionIniciadaResponse(
        Long usuarioId,
        String email,
        Long rolId,
        String rol
) {

    public static final String ROL_ADMIN = "ADMIN";

    public boolean esAdmin() {
        return ROL_ADMIN.equals(rol);
    }
}
