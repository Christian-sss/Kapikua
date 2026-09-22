package com.projects.domain.model.credito;


import com.projects.domain.model.EstadoPrestamo;

import java.math.BigDecimal;

public class Prestamo {


    private Long id;
    private SolicitudCredito solicitudCredito;
    private BigDecimal montoDesembolsado;
    private BigDecimal tcea;
    private BigDecimal saldoCapital;
    private EstadoPrestamo estado;

    public Prestamo(Long id, SolicitudCredito solicitudCredito, BigDecimal montoDesembolsado, BigDecimal tcea, BigDecimal saldoCapital, EstadoPrestamo estado) {
        this.id = id;
        this.solicitudCredito = solicitudCredito;
        this.montoDesembolsado = montoDesembolsado;
        this.tcea = tcea;
        this.saldoCapital = saldoCapital;
        this.estado = estado;
    }

    public Prestamo() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SolicitudCredito getSolicitudCredito() {
        return solicitudCredito;
    }

    public void setSolicitudCredito(SolicitudCredito solicitudCredito) {
        this.solicitudCredito = solicitudCredito;
    }

    public BigDecimal getMontoDesembolsado() {
        return montoDesembolsado;
    }

    public void setMontoDesembolsado(BigDecimal montoDesembolsado) {
        this.montoDesembolsado = montoDesembolsado;
    }

    public BigDecimal getTcea() {
        return tcea;
    }

    public void setTcea(BigDecimal tcea) {
        this.tcea = tcea;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public BigDecimal getSaldoCapital() {
        return saldoCapital;
    }

    public void setSaldoCapital(BigDecimal saldoCapital) {
        this.saldoCapital = saldoCapital;
    }
}
