package com.projects.domain.model.billetera;

import com.projects.domain.result.ClienteError;
import com.projects.domain.result.Result;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.regex.Pattern;

public class Cliente {

    private Long id;
    private String nombres;
    private String apellidos;
    private Long usuarioId;
    private String dni;
    private String numeroCelular;
    private OffsetDateTime fechaRegistro;


    // REGEX PATTERN
    private static final Pattern PATTERN_DNI = Pattern.compile("^\\d{8}$");
    private static final Pattern PATTERN_CELULAR = Pattern.compile("^\\d{9}$");

    private Cliente(Long id, String nombres, String apellidos, String dni, String numeroCelular, Long usuarioId, OffsetDateTime fechaRegistro) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.numeroCelular = numeroCelular;
        this.usuarioId = usuarioId;
        this.fechaRegistro = fechaRegistro;
    }

    public static Result<Cliente> crear(
            Long usuarioId,
            String nombres,
            String apellidos,
            String dni,
            String numeroCelular,
            OffsetDateTime fechaActual
    ) {


        var result = validarDatos(nombres, apellidos, dni, numeroCelular);

        if (result.isFailure()) {
            return Result.failure(result.getError().get());
        }

        if (usuarioId == null) {
            return Result.failure(ClienteError.USUARIO_REQUERIDO.name(), "Es necesario colocar un usuario.");
        }

        if (fechaActual == null) {
            return Result.failure(ClienteError.FECHA_INVALIDA.name(), "La fecha de registro debe ser obligatoria");
        }

        var cliente = new Cliente(
                null,
                nombres.trim(),
                apellidos.trim(),
                dni.trim(),
                numeroCelular.trim(),
                usuarioId,
                fechaActual
        );


        return Result.success(cliente);

    }

    public static Result<Void> validarDatos(String nombres, String apellidos, String dni, String celular) {
        if (nombres == null || nombres.trim().isEmpty()) {
            return Result.failure(ClienteError.NOMBRE_INVALIDO.name(), "Los nombres no pueden estar vacíos");
        }
        if (apellidos == null || apellidos.trim().isEmpty()) {
            return Result.failure(ClienteError.APELLIDO_INVALIDO.name(), "Los apellidos no pueden estar vacíos");
        }
        if (dni == null || !PATTERN_DNI.matcher(dni.trim()).matches()) {
            return Result.failure(ClienteError.DNI_INVALIDO.name(), "El DNI debe tener exactamente 8 dígitos numéricos");
        }
        if (celular == null || !PATTERN_CELULAR.matcher(celular.trim()).matches()) {
            return Result.failure(ClienteError.CELULAR_INVALIDO.name(), "El celular debe tener exactamente 9 dígitos numéricos");
        }
        return Result.success();
    }


    private Cliente() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }


    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public void setNumeroCelular(String numeroCelular) {
        this.numeroCelular = numeroCelular;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
