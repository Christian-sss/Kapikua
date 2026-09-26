package com.projects.domain.model.credito;

import com.projects.domain.model.EstadoSolicitudCredito;
import com.projects.domain.model.billetera.Cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SolicitudCredito {

    private Long id;
    private Cliente cliente;
    private ProductoCrediticio productoCrediticio;
    private BigDecimal montoSolicitado;
    private  Integer plazoMeses;
    private EstadoSolicitudCredito estado;
    private Integer scoreObtenido;
    private String motivoRechazo;
    private LocalDateTime fechaSolicitud;

    public SolicitudCredito() {}

    public SolicitudCredito(Long id, Cliente cliente, ProductoCrediticio productoCrediticio, Integer plazoMeses, BigDecimal montoSolicitado, Integer scoreObtenido, EstadoSolicitudCredito estado, String motivoRechazo, LocalDateTime fechaSolicitud) {
        this.id = id;
        this.cliente = cliente;
        this.productoCrediticio = productoCrediticio;
        this.plazoMeses = plazoMeses;
        this.montoSolicitado = montoSolicitado;
        this.scoreObtenido = scoreObtenido;
        this.estado = estado;
        this.motivoRechazo = motivoRechazo;
        this.fechaSolicitud = fechaSolicitud;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProductoCrediticio getProductoCrediticio() {
        return productoCrediticio;
    }

    public void setProductoCrediticio(ProductoCrediticio productoCrediticio) {
        this.productoCrediticio = productoCrediticio;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }

    public void setMontoSolicitado(BigDecimal montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public Integer getScoreObtenido() {
        return scoreObtenido;
    }

    public void setScoreObtenido(Integer scoreObtenido) {
        this.scoreObtenido = scoreObtenido;
    }

    public EstadoSolicitudCredito getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitudCredito estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }
}
