package com.projects.domain.model.credito;

import com.projects.domain.model.EstadoPrestamo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Modelo simple con los campos de credito.prestamo. Las reglas (desembolso, pagos)
 * viven en el servicio del caso de uso correspondiente.
 */
public class Prestamo {

    private Long id;
    private Long solicitudId;
    private BigDecimal montoDesembolsado;
    private BigDecimal tcea;
    private BigDecimal saldoCapital;
    private EstadoPrestamo estado;
    private OffsetDateTime fechaDesembolso;

    public Prestamo(Long id, Long solicitudId, BigDecimal montoDesembolsado, BigDecimal tcea,
                     BigDecimal saldoCapital, EstadoPrestamo estado, OffsetDateTime fechaDesembolso) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.montoDesembolsado = montoDesembolsado;
        this.tcea = tcea;
        this.saldoCapital = saldoCapital;
        this.estado = estado;
        this.fechaDesembolso = fechaDesembolso;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
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

    public BigDecimal getSaldoCapital() {
        return saldoCapital;
    }

    public void setSaldoCapital(BigDecimal saldoCapital) {
        this.saldoCapital = saldoCapital;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaDesembolso() {
        return fechaDesembolso;
    }

    public void setFechaDesembolso(OffsetDateTime fechaDesembolso) {
        this.fechaDesembolso = fechaDesembolso;
    }
}
