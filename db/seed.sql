-- ============================================================
-- Kapikua - Seed de datos de prueba
-- Ejecutar DESPUÉS de correr el DDL (creación de esquemas/tablas).
-- Orden respetado por dependencias de FK.
--
-- Password de prueba para los 5 usuarios: Kapikua2026!
-- (hash real generado con jBCrypt, cost factor 12 - válido para
--  probar el login con PasswordEncoderAdapter.validar(...))
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- seguridad.rol
-- ------------------------------------------------------------
INSERT INTO seguridad.rol (id, nombre_rol) VALUES
    (1, 'CLIENTE'),
    (2, 'ADMIN'),
    (3, 'ANALISTA_CREDITO'),
    (4, 'SOPORTE'),
    (5, 'AUDITOR');

-- ------------------------------------------------------------
-- seguridad.usuario  (los 5 son rol CLIENTE, para respaldar a
-- los 5 clientes de abajo)
-- ------------------------------------------------------------
INSERT INTO seguridad.usuario (id, rol_id, correo, password_hash, activo) VALUES
    (1, 1, 'juan.perez@kapikua.pe',      '$2a$12$/Sh4bSs8Yi4objjH4lTPAuyYTG3Ng07BhUqmIY5k68aQMXJl66Hqm', TRUE),
    (2, 1, 'maria.lopez@kapikua.pe',     '$2a$12$/Sh4bSs8Yi4objjH4lTPAuyYTG3Ng07BhUqmIY5k68aQMXJl66Hqm', TRUE),
    (3, 1, 'carlos.rodriguez@kapikua.pe','$2a$12$/Sh4bSs8Yi4objjH4lTPAuyYTG3Ng07BhUqmIY5k68aQMXJl66Hqm', TRUE),
    (4, 1, 'ana.torres@kapikua.pe',      '$2a$12$/Sh4bSs8Yi4objjH4lTPAuyYTG3Ng07BhUqmIY5k68aQMXJl66Hqm', TRUE),
    (5, 1, 'luis.garcia@kapikua.pe',     '$2a$12$/Sh4bSs8Yi4objjH4lTPAuyYTG3Ng07BhUqmIY5k68aQMXJl66Hqm', TRUE);

-- ------------------------------------------------------------
-- billetera.cliente
-- ------------------------------------------------------------
INSERT INTO billetera.cliente (id, usuario_id, nombres, apellidos, dni, numero_celular) VALUES
    (1, 1, 'Juan',   'Perez Gomez',      '71234561', '987654321'),
    (2, 2, 'Maria',  'Lopez Sanchez',    '71234562', '987654322'),
    (3, 3, 'Carlos', 'Rodriguez Diaz',   '71234563', '987654323'),
    (4, 4, 'Ana',    'Torres Flores',    '71234564', '987654324'),
    (5, 5, 'Luis',   'Garcia Mendoza',   '71234565', '987654325');

-- ------------------------------------------------------------
-- billetera.billetera
-- ------------------------------------------------------------
INSERT INTO billetera.billetera (id, cliente_id, saldo, estado) VALUES
    (1, 1, 250.00, 'ACTIVA'),
    (2, 2, 320.50, 'ACTIVA'),
    (3, 3,   0.00, 'ACTIVA'),
    (4, 4,  75.25, 'BLOQUEADA'),
    (5, 5, 350.00, 'ACTIVA');

-- ------------------------------------------------------------
-- billetera.tipo_transaccion
-- ------------------------------------------------------------
INSERT INTO billetera.tipo_transaccion (id, codigo, naturaleza) VALUES
    (1, 'DEPOSITO',               'CREDITO'),
    (2, 'RETIRO',                 'DEBITO'),
    (3, 'TRANSFERENCIA_ENVIADA',  'DEBITO'),
    (4, 'TRANSFERENCIA_RECIBIDA', 'CREDITO'),
    (5, 'PAGO_CUOTA',             'DEBITO');

-- ------------------------------------------------------------
-- billetera.transaccion
-- (1-5 movimientos de billetera normales, 6-10 dedicadas a pagos
--  de cuota, porque credito.pago.transaccion_id es UNIQUE)
-- ------------------------------------------------------------
INSERT INTO billetera.transaccion (id, tipo_id, monto, comision, estado) VALUES
    (1, 1, 150.00, 0.00, 'EXITOSA'),
    (2, 1, 320.50, 0.00, 'EXITOSA'),
    (3, 2,  50.00, 2.00, 'EXITOSA'),
    (4, 3, 100.00, 1.00, 'EXITOSA'),
    (5, 4, 100.00, 0.00, 'EXITOSA'),
    (6, 5, 200.00, 0.00, 'EXITOSA'),
    (7, 5, 150.00, 0.00, 'EXITOSA'),
    (8, 5, 180.00, 0.00, 'EXITOSA'),
    (9, 5, 220.00, 0.00, 'EXITOSA'),
    (10,5,  90.00, 0.00, 'EXITOSA');

-- ------------------------------------------------------------
-- billetera.movimiento
-- ------------------------------------------------------------
INSERT INTO billetera.movimiento (id, transaccion_id, billetera_id, signo, monto, saldo_posterior) VALUES
    (1, 1, 1, '+', 150.00, 150.00),
    (2, 2, 2, '+', 320.50, 320.50),
    (3, 3, 5, '-',  50.00, 450.00),
    (4, 4, 5, '-', 100.00, 350.00),
    (5, 5, 1, '+', 100.00, 250.00);

-- ------------------------------------------------------------
-- credito.producto_crediticio
-- ------------------------------------------------------------
INSERT INTO credito.producto_crediticio
    (id, nombre, tipo, monto_minimo, monto_maximo, plazo_minimo_meses, plazo_maximo_meses, tasa_interes_anual)
VALUES
    (1, 'Credito Personal Kapikua', 'CREDITO_PERSONAL',   500.00, 20000.00,  3, 36, 45.00),
    (2, 'Microcredito Kapikua',     'MICROCREDITO',       100.00,  3000.00,  1, 12, 60.00),
    (3, 'Credito Personal Plus',    'CREDITO_PERSONAL',  1000.00, 30000.00,  6, 48, 38.00),
    (4, 'Microcredito Express',     'MICROCREDITO',       100.00,  1500.00,  1,  6, 70.00),
    (5, 'Credito Personal Flex',    'CREDITO_PERSONAL',   300.00, 10000.00,  3, 24, 50.00);

-- ------------------------------------------------------------
-- credito.solicitud_credito
-- (7 filas: 5 APROBADA -> respaldan los 5 prestamos de abajo,
--  1 PENDIENTE y 1 RECHAZADA para probar esos otros caminos)
-- ------------------------------------------------------------
INSERT INTO credito.solicitud_credito
    (id, cliente_id, producto_id, monto_solicitado, plazo_meses, estado, score_obtenido, motivo_rechazo) VALUES
    (1, 1, 1, 3000.00, 12, 'APROBADA',  720, NULL),
    (2, 2, 2,  800.00,  6, 'APROBADA',  650, NULL),
    (3, 3, 3, 5000.00, 24, 'PENDIENTE', NULL, NULL),
    (4, 4, 1, 2000.00, 18, 'RECHAZADA', 480, 'Score insuficiente'),
    (5, 5, 4,  500.00,  3, 'APROBADA',  700, NULL),
    (6, 1, 5, 1500.00,  9, 'APROBADA',  690, NULL),
    (7, 2, 1, 4000.00, 15, 'APROBADA',  710, NULL);

-- ------------------------------------------------------------
-- credito.prestamo  (uno por cada solicitud APROBADA)
-- ------------------------------------------------------------
INSERT INTO credito.prestamo
    (id, solicitud_id, monto_desembolsado, tcea, saldo_capital, estado) VALUES
    (1, 1, 3000.00, 45.00, 3000.00, 'ACTIVO'),
    (2, 2,  800.00, 60.00,  800.00, 'ACTIVO'),
    (3, 5,  500.00, 70.00,    0.00, 'PAGADO'),
    (4, 6, 1500.00, 50.00, 1500.00, 'EN_MORA'),
    (5, 7, 4000.00, 45.00, 4000.00, 'ACTIVO');

-- ------------------------------------------------------------
-- credito.cuota
-- ------------------------------------------------------------
INSERT INTO credito.cuota
    (id, prestamo_id, numero, fecha_vencimiento, capital, interes, mora, estado) VALUES
    (1, 1, 1, '2026-10-15', 250.00, 40.00,  0.00, 'PENDIENTE'),
    (2, 1, 2, '2026-11-15', 250.00, 35.00,  0.00, 'PENDIENTE'),
    (3, 2, 1, '2026-10-10', 133.33, 15.00,  0.00, 'PAGADA'),
    (4, 3, 1, '2026-09-10', 500.00, 20.00,  0.00, 'PAGADA'),
    (5, 4, 1, '2026-08-20', 300.00, 25.00, 15.00, 'VENCIDA');

-- ------------------------------------------------------------
-- credito.pago  (cada uno referencia una transaccion PAGO_CUOTA
-- distinta: 6-10)
-- ------------------------------------------------------------
INSERT INTO credito.pago (id, transaccion_id, monto_total) VALUES
    (1, 6,  200.00),
    (2, 7,  150.00),
    (3, 8,  180.00),
    (4, 9,  220.00),
    (5, 10,  90.00);

-- ------------------------------------------------------------
-- credito.pago_detalle
-- ------------------------------------------------------------
INSERT INTO credito.pago_detalle
    (id, pago_id, cuota_id, aplicado_capital, aplicado_interes, aplicado_mora) VALUES
    (1, 1, 1, 160.00, 40.00,  0.00),
    (2, 2, 2, 115.00, 35.00,  0.00),
    (3, 3, 3, 133.33, 15.00,  0.00),
    (4, 4, 4, 500.00, 20.00,  0.00),
    (5, 5, 5, 300.00, 25.00, 15.00);

-- ------------------------------------------------------------
-- Sincroniza las secuencias SERIAL/BIGSERIAL con los IDs
-- insertados a mano, para que los próximos INSERT desde la app
-- (que no especifican id) no choquen con estos.
-- ------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('seguridad.rol', 'id'), (SELECT MAX(id) FROM seguridad.rol));
SELECT setval(pg_get_serial_sequence('seguridad.usuario', 'id'), (SELECT MAX(id) FROM seguridad.usuario));
SELECT setval(pg_get_serial_sequence('billetera.cliente', 'id'), (SELECT MAX(id) FROM billetera.cliente));
SELECT setval(pg_get_serial_sequence('billetera.billetera', 'id'), (SELECT MAX(id) FROM billetera.billetera));
SELECT setval(pg_get_serial_sequence('billetera.tipo_transaccion', 'id'), (SELECT MAX(id) FROM billetera.tipo_transaccion));
SELECT setval(pg_get_serial_sequence('billetera.transaccion', 'id'), (SELECT MAX(id) FROM billetera.transaccion));
SELECT setval(pg_get_serial_sequence('billetera.movimiento', 'id'), (SELECT MAX(id) FROM billetera.movimiento));
SELECT setval(pg_get_serial_sequence('credito.producto_crediticio', 'id'), (SELECT MAX(id) FROM credito.producto_crediticio));
SELECT setval(pg_get_serial_sequence('credito.solicitud_credito', 'id'), (SELECT MAX(id) FROM credito.solicitud_credito));
SELECT setval(pg_get_serial_sequence('credito.prestamo', 'id'), (SELECT MAX(id) FROM credito.prestamo));
SELECT setval(pg_get_serial_sequence('credito.cuota', 'id'), (SELECT MAX(id) FROM credito.cuota));
SELECT setval(pg_get_serial_sequence('credito.pago', 'id'), (SELECT MAX(id) FROM credito.pago));
SELECT setval(pg_get_serial_sequence('credito.pago_detalle', 'id'), (SELECT MAX(id) FROM credito.pago_detalle));

COMMIT;
