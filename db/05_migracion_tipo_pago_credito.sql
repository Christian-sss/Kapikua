-- Fase 7 (pagar cuota): el plan nombra PAGO_CREDITO al débito que paga una cuota.
-- El catálogo original lo tenía como PAGO_CUOTA; ninguna transacción lo usaba todavía.

UPDATE billetera.tipo_transaccion
    SET codigo = 'PAGO_CREDITO'
    WHERE codigo = 'PAGO_CUOTA';
