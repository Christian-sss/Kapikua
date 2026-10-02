package com.projects.application.dto.response;

public record SesionIniciadaResponse(
        Long usuarioId,
        String email,
        Long rolId,
        String rol,
        String token
) {

    public static final String ROL_ADMIN = "ADMIN";

    // Sesión sin token: no está registrada en la base de datos (p. ej. pantallas de prueba).
    public SesionIniciadaResponse(Long usuarioId, String email, Long rolId, String rol) {
        this(usuarioId, email, rolId, rol, null);
    }

    public boolean esAdmin() {
        return ROL_ADMIN.equals(rol);
    }
}
