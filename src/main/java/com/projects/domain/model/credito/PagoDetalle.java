package com.projects.domain.model.credito;

import java.math.BigDecimal;

/**
 * Modelo simple con los campos de credito.pago_detalle: cuánto de un pago se aplicó a cada
 * concepto (mora, interés, capital) de una cuota.
 */
public class PagoDetalle {

    private Long id;
    private Long pagoId;
    private Long cuotaId;
    private BigDecimal capitalAplicado;
    private BigDecimal interesAplicado;
    private BigDecimal moraAplicado;

    public PagoDetalle(Long id, Long pagoId, Long cuotaId, BigDecimal capitalAplicado,
                        BigDecimal interesAplicado, BigDecimal moraAplicado) {
        this.id = id;
        this.pagoId = pagoId;
        this.cuotaId = cuotaId;
        this.capitalAplicado = capitalAplicado;
        this.interesAplicado = interesAplicado;
        this.moraAplicado = moraAplicado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public void setPagoId(Long pagoId) {
        this.pagoId = pagoId;
    }

    public Long getCuotaId() {
        return cuotaId;
    }

    public void setCuotaId(Long cuotaId) {
        this.cuotaId = cuotaId;
    }

    public BigDecimal getCapitalAplicado() {
        return capitalAplicado;
    }

    public void setCapitalAplicado(BigDecimal capitalAplicado) {
        this.capitalAplicado = capitalAplicado;
    }

    public BigDecimal getInteresAplicado() {
        return interesAplicado;
    }

    public void setInteresAplicado(BigDecimal interesAplicado) {
        this.interesAplicado = interesAplicado;
    }

    public BigDecimal getMoraAplicado() {
        return moraAplicado;
    }

    public void setMoraAplicado(BigDecimal moraAplicado) {
        this.moraAplicado = moraAplicado;
    }
}
