-- Fase 1 (billetera): billetera.transaccion necesita una referencia externa opcional
-- (ej. "BCP ****1234" en un retiro). Nullable porque no todas las transacciones la usan
-- (una transferencia entre billeteras propias del sistema no la necesita).

ALTER TABLE billetera.transaccion
    ADD COLUMN IF NOT EXISTS referencia VARCHAR(255);
