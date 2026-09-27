package com.projects.domain.model.credito;

import com.projects.domain.model.EstadoSolicitudCredito;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Modelo simple con los campos de credito.solicitud_credito. Las reglas de evaluación
 * viven en SolicitarCreditoService, no aquí.
 */
public class SolicitudCredito {

    private Long id;
    private Long clienteId;
    private Long productoId;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private EstadoSolicitudCredito estado;
    private Integer scoreObtenido;
    private String motivoRechazo;
    private OffsetDateTime fechaSolicitud;

    public SolicitudCredito(Long id, Long clienteId, Long productoId, BigDecimal montoSolicitado,
                             Integer plazoMeses, EstadoSolicitudCredito estado, Integer scoreObtenido,
                             String motivoRechazo, OffsetDateTime fechaSolicitud) {
        this.id = id;
        this.clienteId = clienteId;
        this.productoId = productoId;
        this.montoSolicitado = montoSolicitado;
        this.plazoMeses = plazoMeses;
        this.estado = estado;
        this.scoreObtenido = scoreObtenido;
        this.motivoRechazo = motivoRechazo;
        this.fechaSolicitud = fechaSolicitud;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
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

    public EstadoSolicitudCredito getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitudCredito estado) {
        this.estado = estado;
    }

    public Integer getScoreObtenido() {
        return scoreObtenido;
    }

    public void setScoreObtenido(Integer scoreObtenido) {
        this.scoreObtenido = scoreObtenido;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public OffsetDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(OffsetDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
}
