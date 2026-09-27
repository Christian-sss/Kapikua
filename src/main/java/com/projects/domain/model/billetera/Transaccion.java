package com.projects.domain.model.billetera;

import com.projects.domain.model.EstadoTransaccion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Modelo simple con los campos de billetera.transaccion. No valida reglas de negocio:
 * esas viven en el servicio del caso de uso que la construye (TransferirService, etc.).
 */
public class Transaccion {

    private Long id;
    private Long tipoId;
    private BigDecimal monto;
    private BigDecimal comision;
    private EstadoTransaccion estado;
    private OffsetDateTime fecha;
    private String referencia;

    public Transaccion(Long id, Long tipoId, BigDecimal monto, BigDecimal comision,
                        EstadoTransaccion estado, OffsetDateTime fecha, String referencia) {
        this.id = id;
        this.tipoId = tipoId;
        this.monto = monto;
        this.comision = comision;
        this.estado = estado;
        this.fecha = fecha;
        this.referencia = referencia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTipoId() {
        return tipoId;
    }

    public void setTipoId(Long tipoId) {
        this.tipoId = tipoId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public void setComision(BigDecimal comision) {
        this.comision = comision;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
