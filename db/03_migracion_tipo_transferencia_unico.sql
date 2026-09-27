-- Fase 3 (transferencia): el diseño acordado es UNA transacción con DOS movimientos
-- (uno en cada billetera), no dos transacciones separadas. Por eso se unifica
-- TRANSFERENCIA_ENVIADA / TRANSFERENCIA_RECIBIDA en un solo código "TRANSFERENCIA";
-- el signo (+/-) de cada movimiento ya distingue quién envía y quién recibe.

UPDATE billetera.tipo_transaccion
    SET codigo = 'TRANSFERENCIA'
    WHERE codigo = 'TRANSFERENCIA_ENVIADA';

DELETE FROM billetera.tipo_transaccion
    WHERE codigo = 'TRANSFERENCIA_RECIBIDA';
