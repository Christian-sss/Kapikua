package com.projects.domain.model.credito;

import java.math.BigDecimal;

public class PagoDetalle {

    private Long id;
    private Pago pago;
    private Cuota cuota;
    private BigDecimal capitalAplicado;
    private BigDecimal interesAplicado;
    private BigDecimal moraAplicado;

    public PagoDetalle(Long id, BigDecimal moraAplicado, BigDecimal interesAplicado, BigDecimal capitalAplicado, Cuota cuota, Pago pago) {
        this.id = id;
        this.moraAplicado = moraAplicado;
        this.interesAplicado = interesAplicado;
        this.capitalAplicado = capitalAplicado;
        this.cuota = cuota;
        this.pago = pago;
    }


    public PagoDetalle() {}



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cuota getCuota() {
        return cuota;
    }

    public void setCuota(Cuota cuota) {
        this.cuota = cuota;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public BigDecimal getCapitalAplicado() {
        return capitalAplicado;
    }

    public void setCapitalAplicado(BigDecimal capitalAplicado) {
        this.capitalAplicado = capitalAplicado;
    }

    public BigDecimal getMoraAplicado() {
        return moraAplicado;
    }

    public void setMoraAplicado(BigDecimal moraAplicado) {
        this.moraAplicado = moraAplicado;
    }

    public BigDecimal getInteresAplicado() {
        return interesAplicado;
    }

    public void setInteresAplicado(BigDecimal interesAplicado) {
        this.interesAplicado = interesAplicado;
    }
}
