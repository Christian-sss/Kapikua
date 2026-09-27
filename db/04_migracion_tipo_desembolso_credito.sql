-- Fase 5 (crédito): el desembolso de un préstamo aprobado abona el monto a la billetera
-- como una transacción propia, con un único movimiento (+).

INSERT INTO billetera.tipo_transaccion (codigo, naturaleza)
VALUES ('DESEMBOLSO_CREDITO', 'CREDITO')
ON CONFLICT (codigo) DO NOTHING;
