package com.projects.domain.model.billetera;

import com.projects.domain.result.ClienteError;
import com.projects.domain.result.Result;


import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.regex.Pattern;

public class Cliente {


    private Long id;
    private String nombres;
    private String apellidos;
    private Long usuarioId;
    private String dni;
    private String numeroCelular;
    private LocalDate fechaNacimiento;
    private OffsetDateTime fechaRegistro;



    private static final int EDAD_MINIMA = 18;

    // REGEX PATTERN
    private static final Pattern PATTERN_DNI = Pattern.compile("^\\d{8}$");
    private static final Pattern PATTERN_CELULAR = Pattern.compile("^\\d{9}$");

    private Cliente(Long id, String nombres, String apellidos, String dni, String numeroCelular, LocalDate fechaNacimiento, Long usuarioId, OffsetDateTime fechaRegistro) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.numeroCelular = numeroCelular;
        this.fechaNacimiento = fechaNacimiento;
        this.usuarioId = usuarioId;
        this.fechaRegistro = fechaRegistro;
    }

    public static Result<Cliente> crear(
            Long usuarioId,
            String nombres,
            String apellidos,
            String dni,
            String numeroCelular,
            LocalDate fechaNacimiento,
            OffsetDateTime fechaActual
    ) {


        var result = validarDatos(nombres, apellidos, dni, numeroCelular, fechaNacimiento, fechaActual);

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
                fechaNacimiento,
                usuarioId,
                fechaActual
        );


        return Result.success(cliente);

    }

    /**
     * Reconstruye un cliente ya existente (leído de la base de datos), sin pasar por
     * las validaciones de crear(...).
     */
    public static Cliente reconstruir(Long id, Long usuarioId, String nombres, String apellidos, String dni,
                                       String numeroCelular, LocalDate fechaNacimiento, OffsetDateTime fechaRegistro) {
        var cliente = new Cliente();
        cliente.id = id;
        cliente.usuarioId = usuarioId;
        cliente.nombres = nombres;
        cliente.apellidos = apellidos;
        cliente.dni = dni;
        cliente.numeroCelular = numeroCelular;
        cliente.fechaNacimiento = fechaNacimiento;
        cliente.fechaRegistro = fechaRegistro;
        return cliente;
    }

    public static Result<Void> validarDatos(
            String nombres,
            String apellidos,
            String dni,
            String celular,
            LocalDate fechaNacimiento,
            OffsetDateTime fechaActual
    )
    {
        if (nombres == null || nombres.trim().isEmpty()) {
            return Result.failure(ClienteError.NOMBRE_INVALIDO.name(), "Los nombres no pueden estar vacíos");
        }
        if (apellidos == null || apellidos.trim().isEmpty()) {
            return Result.failure(ClienteError.APELLIDO_INVALIDO.name(), "Los apellidos no pueden estar vacíos");
        }
        if (dni == null || !PATTERN_DNI.matcher(dni.trim()).matches()) {
            return Result.failure(ClienteError.DNI_INVALIDO.name(), "El DNI debe tener exactamente 8 dígitos numéricos");
        }

        String dniLimpio = dni.trim();

        if (dniLimpio.chars().distinct().count() == 1) {
            return Result.failure(ClienteError.DNI_INVALIDO.name(), "El DNI no puede tener todos los dígitos idénticos");
        }
        if (esSecuenciaConsecutiva(dniLimpio)) {
            return Result.failure(ClienteError.DNI_INVALIDO.name(), "El DNI no puede ser una secuencia consecutiva");
        }

        if (celular == null || !PATTERN_CELULAR.matcher(celular.trim()).matches()) {
            return Result.failure(ClienteError.CELULAR_INVALIDO.name(), "El celular debe tener exactamente 9 dígitos numéricos");
        }

        String celularLimpio = celular.trim();

        if (celularLimpio.chars().distinct().count() == 1) {
            return Result.failure(ClienteError.CELULAR_INVALIDO.name(), "El celular no puede tener todos los dígitos idénticos");
        }
        if(esSecuenciaConsecutiva(celularLimpio)) {
            return Result.failure(ClienteError.CELULAR_INVALIDO.name(), "El celular no puede ser una secuencia consecutiva");

        }

        var fechaReferencia = (fechaActual != null ? fechaActual : OffsetDateTime.now()).toLocalDate();

        if (fechaNacimiento == null) {
            return Result.failure(ClienteError.FECHA_NACIMIENTO_INVALIDA.name(), "La fecha de nacimiento es obligatoria");
        }
        if (fechaNacimiento.isAfter(fechaReferencia)) {
            return Result.failure(ClienteError.FECHA_NACIMIENTO_INVALIDA.name(), "La fecha de nacimiento no puede ser futura");
        }

        int edad = Period.between(fechaNacimiento, fechaReferencia).getYears();
        if (edad < EDAD_MINIMA) {
            return Result.failure(ClienteError.MENOR_DE_EDAD.name(), "El titular debe ser mayor de " + EDAD_MINIMA + " años");
        }

        return Result.success();
    }



    private static boolean esSecuenciaConsecutiva(String numero) {
        boolean ascendente = true;
        boolean descendente = true;
        for (int i = 0; i < numero.length() - 1; i++) {
            int actual = Character.getNumericValue(numero.charAt(i));
            int siguiente = Character.getNumericValue(numero.charAt(i + 1));
            if (siguiente != actual + 1) {
                ascendente = false;
            }
            if (siguiente != actual - 1) {
                descendente = false;
            }
        }
        return ascendente || descendente;
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

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
