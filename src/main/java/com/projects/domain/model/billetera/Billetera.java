
package com.projects.domain.model.billetera;

import com.projects.domain.model.EstadoBilletera;
import com.projects.domain.result.BilleteraError;
import com.projects.domain.result.Result;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Billetera {

    private Long id;
    private Long clienteId;
    private BigDecimal saldo;
    private EstadoBilletera estadoBilletera;
    private OffsetDateTime fechaCreacion;

    private Billetera(Long id, Long clienteId, OffsetDateTime fechaCreacion, BigDecimal saldo) {
        this.id = id;
        this.clienteId = clienteId;
        this.fechaCreacion = fechaCreacion;
        this.saldo = saldo;
        this.estadoBilletera = EstadoBilletera.ACTIVA;
    }

    /**
     * Reconstruye una billetera ya existente (leída de la base de datos), respetando su
     * estado real. A diferencia de abrirPara(...), no aplica ninguna regla de creación.
     */
    public static Billetera reconstruir(Long id, Long clienteId, BigDecimal saldo, EstadoBilletera estado, OffsetDateTime fechaCreacion) {
        var billetera = new Billetera(id, clienteId, fechaCreacion, saldo);
        billetera.estadoBilletera = estado;
        return billetera;
    }


// Reglas de negocio.

    public static Result<Billetera> abrirPara(
            Long clienteId,
            OffsetDateTime fechaAhora
    ) {

        if (clienteId == null) {
            return Result.failure(BilleteraError.CLIENTE_REQUERIDO.name(), " El cliente debe ser obligatorio.");
        }

        if (fechaAhora == null) {
            return Result.failure(BilleteraError.FECHA_INVALIDA.name(), "La fecha debe ser obligatoria.");
        }


        var nuevaBilletera = new Billetera(
                null,
                clienteId,
                fechaAhora,
                new BigDecimal("0.0")
        );


        return Result.success(nuevaBilletera);

    }


    public Result<Void> deposit(BigDecimal amount) {



        if (estadoBilletera == EstadoBilletera.BLOQUEADA) {
            return Result.failure(BilleteraError.BILLETERA_INACTIVA.name(), "La billetera está bloqueada y no puede operar.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(BilleteraError.AMOUNT_NEGATIVE.name(), "Amount must be greater than zero");
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            return Result.failure(BilleteraError.MONTO_INVALIDO.name(), "El monto admite como máximo 2 decimales.");
        }


        saldo = saldo.add(amount);

        return Result.success();

    }


    public Result<Void> withDraw(BigDecimal amount) {

        if (estadoBilletera == EstadoBilletera.BLOQUEADA) {
            return Result.failure(BilleteraError.BILLETERA_INACTIVA.name(), "La billetera está bloqueada y no puede operar.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(BilleteraError.AMOUNT_NEGATIVE.name(), "Amount must be greater than zero");
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            return Result.failure(BilleteraError.MONTO_INVALIDO.name(), "El monto admite como máximo 2 decimales.");
        }

        if (amount.compareTo(saldo) > 0) {
            return Result.failure(BilleteraError.SALDO_INSUFICIENTE.name(), "Saldo insufficient");
        }


        saldo = saldo.subtract(amount);

        return Result.success();
    }


    // GETTERS AND SETTERS
    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    public EstadoBilletera getEstadoBilletera() {
        return estadoBilletera;
    }

    public void setEstadoBilletera(EstadoBilletera estadoBilletera) {
        this.estadoBilletera = estadoBilletera;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

}

