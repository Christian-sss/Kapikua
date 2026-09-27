package com.projects.domain.model.credito;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Modelo simple con los campos de credito.pago. Las reglas del pago viven en PagarCuotaService.
 */
public class Pago {

    private Long id;
    private Long transaccionId;
    private BigDecimal montoTotal;
    private OffsetDateTime fecha;

    public Pago(Long id, Long transaccionId, BigDecimal montoTotal, OffsetDateTime fecha) {
        this.id = id;
        this.transaccionId = transaccionId;
        this.montoTotal = montoTotal;
        this.fecha = fecha;
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

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public void setFecha(OffsetDateTime fecha) {
        this.fecha = fecha;
    }
}
