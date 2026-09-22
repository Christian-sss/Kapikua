package com.projects.domain.model.credito;

import com.projects.domain.model.EstadoCuota;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Cuota {

    private Long id;

    private Prestamo prestamo;
    private Integer numeroDeCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal mora;
    private EstadoCuota estado;

    public Cuota(Long id, Prestamo prestamo, Integer numeroDeCuota, LocalDate fechaVencimiento, BigDecimal interes, BigDecimal capital, BigDecimal mora, EstadoCuota estado) {
        this.id = id;
        this.prestamo = prestamo;
        this.numeroDeCuota = numeroDeCuota;
        this.fechaVencimiento = fechaVencimiento;
        this.interes = interes;
        this.capital = capital;
        this.mora = mora;
        this.estado = estado;
    }

    public Cuota() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    public Integer getNumeroDeCuota() {
        return numeroDeCuota;
    }

    public void setNumeroDeCuota(Integer numeroDeCuota) {
        this.numeroDeCuota = numeroDeCuota;
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

    public BigDecimal getInteres() {
        return interes;
    }

    public void setInteres(BigDecimal interes) {
        this.interes = interes;
    }
}
