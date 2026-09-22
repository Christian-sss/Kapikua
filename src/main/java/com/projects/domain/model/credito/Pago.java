package com.projects.domain.model.credito;


import com.projects.domain.model.billetera.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pago {
    private Long id;
    private Transaccion transaccion;
    private BigDecimal montoTotal;
    private LocalDateTime fecha;


    public Pago(Long id, BigDecimal montoTotal, Transaccion transaccion, LocalDateTime fecha) {
        this.id = id;
        this.montoTotal = montoTotal;
        this.transaccion = transaccion;
        this.fecha = fecha;
    }

    public Pago() {}



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Transaccion transaccion) {
        this.transaccion = transaccion;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
