package com.projects.domain.model.billetera;

import java.math.BigDecimal;

/**
 * Modelo simple con los campos de billetera.movimiento. Sin reglas de negocio propias.
 */
public class Movimiento {

    private Long id;
    private Long transaccionId;
    private Long billeteraId;
    private Character signo;
    private BigDecimal monto;
    private BigDecimal saldoPosterior;

    public Movimiento(Long id, Long transaccionId, Long billeteraId, Character signo,
                       BigDecimal monto, BigDecimal saldoPosterior) {
        this.id = id;
        this.transaccionId = transaccionId;
        this.billeteraId = billeteraId;
        this.signo = signo;
        this.monto = monto;
        this.saldoPosterior = saldoPosterior;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransaccionId() {
        return transaccionId;
    }

    public void setTransaccionId(Long transaccionId) {
        this.transaccionId = transaccionId;
    }

    public Long getBilleteraId() {
        return billeteraId;
    }

    public void setBilleteraId(Long billeteraId) {
        this.billeteraId = billeteraId;
    }

    public Character getSigno() {
        return signo;
    }

    public void setSigno(Character signo) {
        this.signo = signo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public BigDecimal getSaldoPosterior() {
        return saldoPosterior;
    }

    public void setSaldoPosterior(BigDecimal saldoPosterior) {
        this.saldoPosterior = saldoPosterior;
    }
}
