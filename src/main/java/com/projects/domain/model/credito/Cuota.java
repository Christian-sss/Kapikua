package com.projects.domain.model.credito;

import com.projects.domain.model.EstadoCuota;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo simple con los campos de credito.cuota.
 */
public class Cuota {

    private Long id;
    private Long prestamoId;
    private Integer numero;
    private LocalDate fechaVencimiento;
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal mora;
    private EstadoCuota estado;

    public Cuota(Long id, Long prestamoId, Integer numero, LocalDate fechaVencimiento,
                  BigDecimal capital, BigDecimal interes, BigDecimal mora, EstadoCuota estado) {
        this.id = id;
        this.prestamoId = prestamoId;
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
        this.capital = capital;
        this.interes = interes;
        this.mora = mora;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPrestamoId() {
        return prestamoId;
    }

    public void setPrestamoId(Long prestamoId) {
        this.prestamoId = prestamoId;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public BigDecimal getInteres() {
        return interes;
    }

    public void setInteres(BigDecimal interes) {
        this.interes = interes;
    }

    public BigDecimal getMora() {
        return mora;
    }

    public void setMora(BigDecimal mora) {
        this.mora = mora;
    }

    public EstadoCuota getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuota estado) {
        this.estado = estado;
    }
}
