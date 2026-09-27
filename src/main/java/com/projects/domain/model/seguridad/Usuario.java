package com.projects.domain.model.seguridad;

import com.projects.domain.result.Result;
import com.projects.domain.result.UsuarioError;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.regex.Pattern;

public class Usuario {

    private Long id;
    private Long rolId;
    private String email;
    private String passwordHash;
    private Boolean activo;
    private OffsetDateTime fechaCreacion;



    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final Set<String> DOMINIOS_PERMITIDOS = Set.of(
            "upt.pe",
            "virtual.upt.pe",
            "gmail.com",
            "outlook.com"
    );

    private Usuario(Long id, Long rolId, String passwordHash, String email, OffsetDateTime fechaCreacion) {
        this.id = id;
        this.rolId = rolId;
        this.passwordHash = passwordHash;
        this.email = email;
        this.activo = true;
        this.fechaCreacion = fechaCreacion;
    }

    public static Result<Usuario> crear(
            String email,
            String passwordHash,
            Long rolId,
            OffsetDateTime fechaCreacion
    ) {

        var resultValidacion =  validarDatos(email,passwordHash);


        if (rolId == null) {
            return Result.failure(UsuarioError.ROL_REQUERIDO.name(), "Es necesario colocar un rol.");
        }

        if (fechaCreacion == null) {
            return Result.failure(UsuarioError.FECHA_INVALIDA .name(), "La fecha de registro debe ser obligatoria");
        }



        if(resultValidacion.isFailure()) {
            return Result.failure(resultValidacion.getError().get());
        }

        var usuario = new  Usuario(
                null,
                rolId,
                passwordHash.trim(),
                email.trim().toLowerCase(),
                fechaCreacion
        );


        return Result.success(usuario);
    }

    public static Usuario reconstruir(
            Long id,
            Long rolId,
            String email,
            String passwordHash,
            Boolean activo,
            OffsetDateTime fechaCreacion
    ) {
        var usuario = new Usuario();
        usuario.id = id;
        usuario.rolId = rolId;
        usuario.email = email;
        usuario.passwordHash = passwordHash;
        usuario.activo = activo;
        usuario.fechaCreacion = fechaCreacion;
        return usuario;
    }

    public static Result<Void> validarDatos(String email, String passwordHash) {


        if (email == null || email.trim().isEmpty()) {
            return Result.failure(UsuarioError.EMAIL_INVALIDO.name(), "El campo email debe ser obligatorio");
        }


        if(!EMAIL_PATTERN.matcher(email).matches()) {
            return Result.failure(UsuarioError.EMAI_FORMAT_IMVALIDO.name(), "Formato del email invalido");
        }

        String dominio = email.trim().toLowerCase().substring(email.indexOf('@') + 1);
        if (!DOMINIOS_PERMITIDOS.contains(dominio)) {
            return Result.failure(UsuarioError.EMAIL_DOMINIO_NO_PERMITIDO.name(),
                    "El dominio del email debe ser uno de: " + String.join(", ", DOMINIOS_PERMITIDOS));
        }


        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            return Result.failure(UsuarioError.PASSWORD_INVALIDO.name(), "El campo password debe ser obligatorio");
        }

        return Result.success();

    }




    private Usuario() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRolId() {
        return rolId;
    }

    public void setRolId(Long rolId) {
        this.rolId = rolId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
