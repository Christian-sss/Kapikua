package com.projects.domain.model.billetera;
import java.math.BigDecimal;

public class Movimiento {
    private Long id;
    private Transaccion transaccion;
    private Billetera billetera;
    private Character signo;
    private BigDecimal monto;
    private BigDecimal saldoPosterior;


    public Movimiento(Long id, Transaccion transaccion, Billetera billetera, BigDecimal monto, Character signo, BigDecimal saldoPosterior) {
        this.id = id;
        this.transaccion = transaccion;
        this.billetera = billetera;
        this.monto = monto;
        this.signo = signo;
        this.saldoPosterior = saldoPosterior;
    }


    public Movimiento() {}


    public Transaccion getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Transaccion transaccion) {
        this.transaccion = transaccion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Billetera getBilletera() {
        return billetera;
    }

    public void setBilletera(Billetera billetera) {
        this.billetera = billetera;
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
