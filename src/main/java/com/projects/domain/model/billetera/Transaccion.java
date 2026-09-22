package com.projects.domain.model.billetera;
import com.projects.domain.model.EstadoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaccion {

    private Long id;
    private Billetera billeteraOrigen;
    private Billetera billeteraDestino;
    private TipoTransaccion tipoTransaccion;
    private BigDecimal amount;
    private LocalDateTime fecha;
    private EstadoTransaccion estadoTransaccion;


    public Transaccion(Long id, EstadoTransaccion estadoTransaccion, LocalDateTime fecha, TipoTransaccion tipoTransaccion, Billetera billeteraDestino, Billetera billeteraOrigen, BigDecimal amount) {
        this.id = id;
        this.estadoTransaccion = estadoTransaccion;
        this.fecha = fecha;
        this.tipoTransaccion = tipoTransaccion;
        this.billeteraDestino = billeteraDestino;
        this.billeteraOrigen = billeteraOrigen;
        this.amount = amount;
    }

    public Transaccion() {
    }

    public Long getId() {
        return id;
    }

    public EstadoTransaccion getEstadoTransaccion() {
        return estadoTransaccion;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }

    public Billetera getBilleteraDestino() {
        return billeteraDestino;
    }

    public Billetera getBilleteraOrigen() {
        return billeteraOrigen;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}


