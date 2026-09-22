package com.projects.domain.model.credito;

import com.projects.domain.model.TipoProductoCrediticio;

import java.math.BigDecimal;

public class ProductoCrediticio {

    private Long id;
    private String nombre;
    private TipoProductoCrediticio tipo;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private Integer plazoMinimoMeses;
    private Integer plazoMaximoMeses;
    private BigDecimal tasaInteresAnual;


    public ProductoCrediticio(Long id, String nombre, BigDecimal montoMinimo, BigDecimal montoMaximo, TipoProductoCrediticio tipo, Integer plazoMinimoMeses, Integer plazoMaximoMeses, BigDecimal tasaInteresAnual) {
        this.id = id;
        this.nombre = nombre;
        this.montoMinimo = montoMinimo;
        this.montoMaximo = montoMaximo;
        this.tipo = tipo;
        this.plazoMinimoMeses = plazoMinimoMeses;
        this.plazoMaximoMeses = plazoMaximoMeses;
        this.tasaInteresAnual = tasaInteresAnual;
    }


    public ProductoCrediticio() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getMontoMaximo() {
        return montoMaximo;
    }

    public void setMontoMaximo(BigDecimal montoMaximo) {
        this.montoMaximo = montoMaximo;
    }

    public Integer getPlazoMinimoMeses() {
        return plazoMinimoMeses;
    }

    public void setPlazoMinimoMeses(Integer plazoMinimoMeses) {
        this.plazoMinimoMeses = plazoMinimoMeses;
    }

    public Integer getPlazoMaximoMeses() {
        return plazoMaximoMeses;
    }

    public void setPlazoMaximoMeses(Integer plazoMaximoMeses) {
        this.plazoMaximoMeses = plazoMaximoMeses;
    }

    public BigDecimal getTasaInteresAnual() {
        return tasaInteresAnual;
    }

    public void setTasaInteresAnual(BigDecimal tasaInteresAnual) {
        this.tasaInteresAnual = tasaInteresAnual;
    }

    public BigDecimal getMontoMinimo() {
        return montoMinimo;
    }

    public void setMontoMinimo(BigDecimal montoMinimo) {
        this.montoMinimo = montoMinimo;
    }

    public TipoProductoCrediticio getTipo() {
        return tipo;
    }

    public void setTipo(TipoProductoCrediticio tipo) {
        this.tipo = tipo;
    }
}
