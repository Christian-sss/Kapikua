
package com.projects.domain.model.billetera;

import com.projects.domain.model.EstadoBilletera;
import com.projects.domain.result.BilleteraError;
import com.projects.domain.result.Result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Billetera {

    private Long id;
    private Cliente cliente;
    private BigDecimal saldo;
    private EstadoBilletera estadoBilletera;
    private LocalDateTime fechaCreacion;

    public Billetera(Long id, LocalDateTime fechaCreacion, EstadoBilletera estadoBilletera, BigDecimal saldo, Cliente cliente) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.estadoBilletera = EstadoBilletera.ACTIVA;
        this.saldo = saldo;
        this.cliente = cliente;
    }


// Reglas de negocio.


    public Result<Void> deposit(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(BilleteraError.AMOUNT_NEGATIVE.name(), "Amount must be greater than zero");
        }

        saldo = saldo.add(amount);

        return Result.success();

    }


    public Result<Void> withDraw(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.failure(BilleteraError.AMOUNT_NEGATIVE.name(), "Amount must be greater than zero");
        }

        if (amount.compareTo(saldo) > 0) {
            return Result.failure(BilleteraError.SALDO_INSUFICIENTE.name(), "Saldo insufficient");
        }

        saldo = saldo.subtract(amount);

        return Result.success();
    }


    // GETTERS AND SETTERS

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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

