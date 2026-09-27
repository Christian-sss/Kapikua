# Kapikua: Análisis y plan del proyecto

> Billetera digital con créditos y microcréditos.
> Java 21 · Swing · PostgreSQL · Arquitectura hexagonal (puertos y adaptadores) · Sin frameworks
> Documento generado el 2026-09-22 a partir de las sesiones de análisis.

## Índice

1. [Contexto y alcance](#1-contexto-y-alcance)
2. [Análisis del estado actual del código](#2-análisis-del-estado-actual-del-código)
3. [Observaciones sobre la base de datos](#3-observaciones-sobre-la-base-de-datos)
4. [Requerimientos funcionales generales](#4-requerimientos-funcionales-generales)
5. [Requerimientos del módulo de crédito](#5-requerimientos-del-módulo-de-crédito)
6. [Domain Services](#6-domain-services)
7. [Decisiones del equipo: sin recargas y retiro](#7-decisiones-del-equipo-sin-recargas-y-retiro)
8. [Alcance final de la Unidad 1](#8-alcance-final-de-la-unidad-1)
9. [Casos de uso de la Unidad 1](#9-casos-de-uso-de-la-unidad-1)
10. [Plan de 5 días](#10-plan-de-5-días)
11. [Pautas de Swing](#11-pautas-de-swing)
12. [Preparación para la Unidad 2 (Spring)](#12-preparación-para-la-unidad-2-spring)
13. [Riesgos](#13-riesgos)
14. [Backlog para las Unidades 2 y 3](#14-backlog-para-las-unidades-2-y-3)

---

## 1. Contexto y alcance

- **Unidad 1 (actual, 5 días):** aplicación de escritorio en Swing. El objetivo es la **billetera digital completa** y un **mínimo funcional de créditos y microcréditos**.
- **Unidades 2 y 3:** versión web con Spring. Si `domain` y `application` quedan limpios ahora, se reutilizan sin cambios.

---

## 2. Análisis del estado actual del código

### Lo que está bien

- La estructura hexagonal está bien planteada: `domain` no depende de nada, `application` define los puertos e `infrastructure` los implementa.
- `Result<T>` con interfaz `sealed` (`Success` / `Failure`) es una buena base para manejar errores sin excepciones.
- Los enums de estado coinciden en su mayoría con los `CHECK` de la BD.

### Errores concretos

| # | Dónde | Problema |
|---|---|---|
| 1 | `application/port/in/RegistrarUsuarioUseCase.java` | `ejecutar(RegistrarUsuarioUseCase usuarioUseCase)` recibe la propia interfaz en lugar de `RegistrarUsuarioCommand`. |
| 2 | `application/command/RegistrarUsuarioCommand.java` | Solo tiene `email`, `telefono` y `password`, pero `cliente` exige `nombres`, `apellidos` y `dni` (NOT NULL). El registro debe crear usuario + cliente + billetera y asignar un `rol_id`. |
| 3 | `application/command/UsuarioRegistradoCommand.java` | Es una respuesta, no un comando. Renombrarlo a `UsuarioRegistradoResponse`. |
| 4 | `pom.xml` | Falta el driver JDBC de PostgreSQL (`org.postgresql:postgresql`). |
| 5 | `domain/model/billetera/Billetera.java` (constructor) | Ignora el parámetro `estadoBilletera` y siempre asigna `ACTIVA`. |
| 6 | `domain/model/EstadoBilletera.java` | Tiene `INACTIVA`, pero el `CHECK` de la BD solo admite `ACTIVA` o `BLOQUEADA`. |
| 7 | `Billetera.deposit` / `withDraw` | No verifican si la billetera está `BLOQUEADA`, aunque `DomainError.billeteraInactiva()` ya existe. |
| 8 | `infrastructure/adapter/out/persistence/PgPersistenceAdapter.java` | Captura la `SQLException`, llama a `printStackTrace()` y devuelve `Optional.empty()`. El error se pierde. |
| 9 | `Main.java` | Sigue siendo la plantilla de IntelliJ. `Login` tiene su propio `main` y configura Nimbus y luego FlatLaf (Nimbus sobra). |

### Transacciones de BD (el problema de arquitectura principal)

Cada `save()` abre su propia conexión, pero casi todos los casos de uso escriben en varias tablas que deben confirmarse juntas:

- **Registro:** usuario → cliente → billetera
- **Transferencia:** transacción → 2 movimientos → 2 actualizaciones de saldo
- **Desembolso:** préstamo → cuotas → transacción → movimiento → saldo
- **Pago de cuota:** transacción → movimiento → pago → pago_detalle → cuota → préstamo

**Solución sin frameworks:** un puerto de salida `TransactionManager`:

```java
// application/port/out
public interface TransactionManager {
    <T> Result<T> inTransaction(Supplier<Result<T>> work);
}
```

El adaptador JDBC guarda la `Connection` en un `ThreadLocal`, llama a `setAutoCommit(false)` y hace `commit` o `rollback` según el `Result`. Los repositorios toman la conexión del `ThreadLocal`.

**Concurrencia en saldos:** usar `SELECT ... FOR UPDATE` sobre las billeteras, bloqueándolas **siempre en orden de id** para evitar deadlocks. La alternativa es `UPDATE billetera SET saldo = saldo - ? WHERE id = ? AND saldo >= ?` comprobando cuántas filas se actualizaron.

### El dominio no coincide con la BD

- **`Transaccion`** tiene `billeteraOrigen` y `billeteraDestino`, pero en la BD eso vive en `movimiento` (partida doble). Hay que modelar la `Transaccion` con una lista de `Movimiento` y agregar `comision`.
- **`TIMESTAMPTZ` → `LocalDateTime`** pierde la zona horaria. Usar `OffsetDateTime` o `Instant`.
- **Referencias entre agregados:** `Billetera → Cliente → Usuario`, `Cuota → Prestamo`, `Movimiento → Billetera`. Entre agregados hay que referenciar **por id**; dentro de un agregado sí se guardan objetos (`Prestamo` con su `List<Cuota>`).
- **`Movimiento.signo` como `Character`:** usar un enum `Signo { ABONO('+'), CARGO('-') }`.

### Dominio anémico

Todas las entidades tienen constructor vacío público y setters en todos los campos. Las reglas de negocio deben vivir en el dominio:

- `Cliente`: validar DNI (`^\d{8}$`) y celular (`^\d{9}$`) en un método de fábrica que devuelva `Result<Cliente>`.
- `ProductoCrediticio`: `validarCondiciones(monto, plazo)`.
- `SolicitudCredito`: `aprobar()` / `rechazar(motivo)`, solo si está `PENDIENTE`.
- `Prestamo`: `pagarSiguienteCuota()` y los cambios de estado.
- `Billetera`: `debitar()` / `acreditar()` validando el estado y el saldo.
- Dinero: `BigDecimal` con `setScale(2, RoundingMode.HALF_EVEN)`. Un value object `Dinero` es opcional.

**Mejoras en `Result`:** hacer `Success` pública (hoy es package-private y `Failure` es pública) para usar `switch` con pattern matching. Las ramas `"Estado desconocido"` sobran porque la interfaz es `sealed`. `Rol.validarRol` usa `Result.failure("","")`, un error vacío.

### Infraestructura

- **Contraseñas sin librerías:** PBKDF2 del JDK (`SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")`), salt con `SecureRandom`, guardar `iteraciones:salt:hash` en `password_hash` y comparar con `MessageDigest.isEqual`.
- **`PostressqlConexion`** (el nombre tiene una errata): leer URL, usuario y contraseña de un `config.properties` ignorado en git o de variables de entorno.
- **Un adaptador por puerto:** `JdbcUsuarioRepository`, `JdbcBilleteraRepository`, etc., en lugar de un `PgPersistenceAdapter` que implemente todo.
- **Swing:** las pantallas solo conocen los puertos de entrada (`*UseCase`).
- **Composition root:** `Main` es el único lugar donde se crean las piezas y se conectan.

### Estructura de paquetes recomendada

```
com.projects
├── Main.java                      ← composition root
├── domain
│   ├── shared/     Result, DomainError, (Dinero)
│   ├── seguridad/  Usuario, Rol
│   ├── billetera/
│   │   ├── model/    Cliente, Billetera, Transaccion, Movimiento, TipoTransaccion
│   │   └── service/  ServicioTransferencia
│   └── credito/
│       ├── model/    ProductoCrediticio, SolicitudCredito, Prestamo, Cuota, Pago, PagoDetalle,
│       │             Cronograma, CuotaProyectada, PerfilCrediticio   (value objects)
│       └── service/  CalculadoraCronograma, EvaluadorCrediticio
├── application
│   ├── port/in/    *UseCase
│   ├── port/out/   *Repository, *Query, PasswordHasher, TransactionManager
│   ├── dto/        comandos y respuestas (records)
│   └── service/    una implementación por caso de uso
└── infrastructure
    ├── adapter/in/swing/         pantallas + SesionActual
    ├── adapter/out/persistence/  Jdbc*Repository + mappers
    ├── adapter/out/security/     Pbkdf2PasswordHasher
    └── config/                   conexión, JdbcTransactionManager, config.properties
```

---

## 3. Observaciones sobre la base de datos

1. **El script tiene `SELECT * FROM billetera.billetera;` antes del `CREATE TABLE billetera.billetera`**, así que falla si se ejecuta completo. Quitar los `SELECT` sueltos.
2. **PostgreSQL no crea índices en las claves foráneas.** Indexar `movimiento(billetera_id)`, `movimiento(transaccion_id)`, `solicitud_credito(cliente_id)`, `pago_detalle(pago_id)` y `pago_detalle(cuota_id)`.
3. **Faltan `CHECK`:** `comision >= 0`, `monto_solicitado > 0`, `plazo_meses > 0`, `tasa_interes_anual >= 0`, y `capital`, `interes` y `mora >= 0`.
4. **Precisión inconsistente:** la billetera usa `NUMERIC(12,2)` y el crédito `NUMERIC(10,2)`. Unificar en `(12,2)`.
5. **`tipo_transaccion.naturaleza`** no encaja con las transferencias (son débito para una billetera y crédito para la otra). La columna `movimiento.signo` ya lo cubre.
6. **Datos semilla necesarios** (ver la sección 8).

### Cambios sugeridos al esquema `credito` (para la Unidad 2; ninguno es obligatorio en la Unidad 1)

| # | Cambio | Por qué |
|---|---|---|
| 1 | `prestamo.tasa_interes_anual NUMERIC(5,2) NOT NULL` | El préstamo debe conservar su tasa aunque se edite el producto |
| 2 | `prestamo.transaccion_desembolso_id BIGINT UNIQUE REFERENCES billetera.transaccion` | Enlazar el préstamo con el dinero que recibió el cliente |
| 3 | `pago.prestamo_id BIGINT NOT NULL REFERENCES credito.prestamo` | Consultar los pagos sin pasar por las cuotas |
| 4 | `producto_crediticio.activo BOOLEAN DEFAULT TRUE` | Desactivar productos |
| 5 | `producto_crediticio.tasa_moratoria_anual NUMERIC(5,2)` | Cálculo de mora |
| 6 | `solicitud_credito.fecha_evaluacion` y `evaluado_por` | Trazabilidad de la aprobación manual |
| 7 | Índices en `cuota(estado, fecha_vencimiento)`, `solicitud_credito(cliente_id, estado)` y `prestamo(estado)` | Proceso de mora y filtros |
| 8 | Opcional: `score_minimo`, `aprobacion_automatica` en el producto | Mover la política del código a la BD |
| 9 | Opcional: `transaccion.referencia` o `descripcion` | Guardar el banco o cuenta de destino de un retiro |

---

## 4. Requerimientos funcionales generales

**Prioridad:** 🟢 Esencial · 🟡 Recomendado · ⚪ Opcional · ❌ Descartado por el equipo

### Seguridad y acceso
| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-01 | Registrar un usuario con correo y contraseña | 🟢 |
| RF-02 | Iniciar sesión validando las credenciales | 🟢 |
| RF-03 | Cerrar sesión | 🟢 |
| RF-04 | Rechazar el acceso a usuarios desactivados | 🟢 |
| RF-05 | Controlar el acceso según el rol (CLIENTE / ADMIN) | 🟡 |
| RF-06 | Cambiar la contraseña | 🟡 |
| RF-07 | Bloqueo temporal tras N intentos fallidos | ⚪ |
| RF-08 | Recuperar la contraseña | ⚪ |

### Cliente
| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-09 | Registrar los datos personales: nombres, apellidos, DNI y celular | 🟢 |
| RF-10 | Validar el formato y la unicidad del DNI (8 dígitos) y del celular (9 dígitos) | 🟢 |
| RF-11 | Consultar y editar el perfil | 🟡 |
| RF-12 | Verificación de identidad (KYC) | ⚪ |

### Billetera
| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-13 | Crear la billetera automáticamente al registrarse (una por cliente, saldo 0) | 🟢 |
| RF-14 | Consultar el saldo | 🟢 |
| RF-15 | Bloquear o desbloquear la billetera | 🟡 |
| RF-16 | Impedir operaciones en una billetera `BLOQUEADA` | 🟢 |
| RF-17 | No permitir nunca un saldo negativo | 🟢 |

### Operaciones de dinero
| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-18 | Recargar o depositar saldo | ❌ Descartado (sección 7) |
| RF-19 | Retirar saldo a una cuenta bancaria (simulado) | 🟡 |
| RF-20 | Transferir a otro cliente por número de celular | 🟢 |
| RF-21 | Mostrar el nombre enmascarado del destinatario antes de confirmar | 🟡 |
| RF-22 | Impedir transferencias a la propia billetera | 🟢 |
| RF-23 | Límites por operación y diarios | 🟡 |
| RF-24 | Comisiones por tipo de operación | ⚪ |
| RF-25 | Operaciones atómicas (se completan enteras o no se aplica nada) | 🟢 |
| RF-26 | Registrar cada operación como transacción con sus movimientos y el saldo posterior | 🟢 |
| RF-27 | Generar un comprobante | 🟡 |
| RF-28 | Pago de servicios o QR | ⚪ |

### Consultas
| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-29 | Historial de movimientos | 🟢 |
| RF-30 | Filtrar por fechas y tipo | 🟡 |
| RF-31 | Detalle de una transacción | 🟡 |
| RF-32 | Exportar estado de cuenta | ⚪ |

### Administración y otros
| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-46 | Gestionar productos crediticios | 🟡 |
| RF-47 | Bandeja de solicitudes pendientes | 🟡 |
| RF-48 | Buscar clientes, activar o desactivar usuarios, bloquear billeteras | 🟡 |
| RF-49 | Reportes (cartera, morosidad, volumen) | ⚪ |
| RF-50 | Notificaciones dentro de la aplicación | ⚪ |
| RF-51 | Log de auditoría | ⚪ |

> Los RF-33 a RF-45 (crédito) se detallan en la sección 5 como RFC-xx.

---

## 5. Requerimientos del módulo de crédito

### 5.1 Decisiones de diseño

| # | Decisión | Resolución |
|---|---|---|
| D1 | ¿Aprobar y desembolsar son pasos separados? | **No: van en la misma transacción de BD.** Una solicitud `APROBADA` siempre tiene su `prestamo`. |
| D2 | ¿Qué significa `prestamo.estado = CANCELADO`? | **Cancelación anticipada** (pagado antes del plazo). `PAGADO` significa que terminó según el cronograma. |
| D3 | ¿De dónde sale la tasa del préstamo? | En la Unidad 1 los productos no se editan, así que se lee del producto. En la Unidad 2, guardar una copia en `prestamo`. |
| D4 | ¿Quién evalúa? | Unidad 1: automático para ambos tipos. Unidad 2: microcrédito automático, crédito personal con revisión del admin. |
| D5 | Frecuencia de pago | Mensual (el esquema usa `plazo_*_meses`). |
| D6 | Simular el paso del tiempo | Puerto `Clock` con fecha simulada configurable (Unidad 2, para la mora). |

### 5.2 Ciclo de vida

```
Ver catálogo ──► Simular ──► Solicitar ──► [Filtros de evaluación]
                                         │ falla → RECHAZADA (motivo_rechazo)
                                         ▼
                          APROBADA + DESEMBOLSO (una sola transacción de BD)
                          prestamo + N cuotas + transaccion + movimiento(+)
                                         ▼
                    Pagos del cliente  ◄──►  (Unidad 2) proceso de mora
                                         ▼
                              PAGADO  |  CANCELADO (anticipado)
```

### 5.3 Máquinas de estado

| Entidad | Transición | Disparador |
|---|---|---|
| `solicitud_credito` | `PENDIENTE → APROBADA` | Evaluación favorable |
| | `PENDIENTE → RECHAZADA` | Falla un filtro; `motivo_rechazo` obligatorio |
| | `APROBADA`, `RECHAZADA` | Estados finales |
| `prestamo` | `ACTIVO → EN_MORA` | Al menos una cuota `VENCIDA` (Unidad 2) |
| | `EN_MORA → ACTIVO` | Un pago deja al préstamo sin cuotas vencidas |
| | `ACTIVO/EN_MORA → PAGADO` | Se paga la última cuota |
| | `ACTIVO/EN_MORA → CANCELADO` | Cancelación anticipada (Unidad 2) |
| `cuota` | `PENDIENTE → VENCIDA` | `fecha_vencimiento < hoy` |
| | `PENDIENTE/VENCIDA → PAGADA` | Un pago la cubre por completo |

Las transiciones las ejecuta el **dominio** (`solicitud.aprobar()`, `cuota.marcarVencida()`), no los setters.

### 5.4 Microcrédito vs crédito personal

| Característica | MICROCREDITO | CREDITO_PERSONAL |
|---|---|---|
| Monto (ejemplo para el seed) | S/ 100 – 2 000 | S/ 2 000 – 30 000 |
| Plazo | 1 – 6 meses | 6 – 36 meses |
| TEA (ejemplo) | 60 – 90 % | 25 – 45 % |
| **Regla de la Unidad 1** | Sin requisitos previos | **Exige haber pagado al menos un microcrédito** |
| Unidad 2 | Aprobación automática, score mínimo bajo | Revisión manual, score alto, capacidad de pago ≤ 40 % del ingreso |
| Préstamos activos simultáneos | Máximo 1 | Máximo 1 |

> Son valores de ejemplo, no cifras reguladas. Para presentarlo como un caso peruano, revisar los topes de tasa vigentes del BCRP.

### 5.5 Requerimientos RFC

La columna **U1** indica si entra en la Unidad 1.

#### A. Catálogo
| ID | Requerimiento | Tablas | U1 |
|---|---|---|---|
| RFC-01 | Listar productos (nombre, tipo, rangos, TEA) | `producto_crediticio` | ✅ |
| RFC-02 | Ver el detalle de un producto | `producto_crediticio` | ✅ |

#### B. Simulación (no guarda nada)
| ID | Requerimiento | Tablas | U1 |
|---|---|---|---|
| RFC-03 | Simular: cuota, total de intereses, total a pagar, TEA, TEM y cronograma completo | Solo lee `producto_crediticio` | ✅ |
| RFC-04 | Fecha de vencimiento de cada cuota (`plusMonths`) | — | ✅ |

#### C. Solicitud
| ID | Requerimiento | Tablas | U1 |
|---|---|---|---|
| RFC-05 | Registrar la solicitud (producto, monto, plazo) | `solicitud_credito` | ✅ |
| RFC-06 | Filtros de rechazo directo | `billetera`, `solicitud_credito`, `prestamo` | ✅ (versión simple) |
| RFC-07 | Consultar mis solicitudes | `solicitud_credito` | 🟡 |

**Catálogo de `motivo_rechazo`:**

| Código | Condición | U1 |
|---|---|---|
| `FUERA_DE_RANGO` | Monto o plazo fuera del rango del producto | ✅ |
| `BILLETERA_BLOQUEADA` | La billetera está `BLOQUEADA` | ✅ |
| `LIMITE_PRESTAMOS` | Ya tiene un préstamo `ACTIVO` del mismo tipo | ✅ |
| `PRESTAMO_EN_MORA` | Tiene algún préstamo `EN_MORA` | ✅ |
| `REQUIERE_MICROCREDITO_PAGADO` | Pide un crédito personal sin haber pagado un microcrédito | ✅ |
| `USUARIO_INACTIVO` | `usuario.activo = false` | U2 |
| `SOLICITUD_PENDIENTE` | Ya tiene una solicitud `PENDIENTE` | U2 |
| `ANTIGUEDAD_INSUFICIENTE` | Cuenta más nueva que el mínimo del tipo | U2 |
| `SCORE_INSUFICIENTE` | Score bajo el umbral | U2 |
| `CAPACIDAD_PAGO` | La cuota supera el % permitido del ingreso | U2 |

#### D. Evaluación
| ID | Requerimiento | U1 |
|---|---|---|
| RFC-08 | Calcular el score (0–1000) | U2 (`score_obtenido` queda `NULL`) |
| RFC-09 | Aprobación automática | ✅ (ambos tipos) |
| RFC-10 | Bandeja del admin para el crédito personal | U2 |
| RFC-11 | Una solicitud solo se evalúa una vez | ✅ |

#### E. Desembolso (una sola transacción de BD)
| ID | Requerimiento | Tablas | U1 |
|---|---|---|---|
| RFC-12 | Crear el préstamo (`monto_desembolsado = monto_solicitado`, `saldo_capital = monto`, `tcea`, `ACTIVO`) | `prestamo` | ✅ |
| RFC-13 | Generar N cuotas con el método francés (la última absorbe el redondeo) | `cuota` | ✅ |
| RFC-14 | Abonar a la billetera | `transaccion` (`DESEMBOLSO_CREDITO`), `movimiento` (+), `billetera` | ✅ |
| RFC-15 | Solicitud → `APROBADA`; rollback completo si algo falla | `solicitud_credito` | ✅ |

#### F. Consultas
| ID | Requerimiento | U1 |
|---|---|---|
| RFC-16 | Mis préstamos (estado, monto, saldo de capital, próximo vencimiento) | ✅ |
| RFC-17 | Cronograma con lo pagado y lo pendiente por cuota | ✅ |
| RFC-18 | Deuda exigible hoy y deuda total para cancelar | U2 |
| RFC-19 | Historial de pagos y su distribución | U2 |

#### G. Pagos
| ID | Requerimiento | U1 |
|---|---|---|
| RFC-20 | Pagar con saldo: próxima cuota / cuotas vencidas / monto libre | ✅ **solo la próxima cuota completa** |
| RFC-21 | Orden: cuotas por `numero` ascendente; dentro de cada una, mora → interés → capital | ✅ (una cuota por vez) |
| RFC-22 | Actualizar estados: cuota `PAGADA`, `saldo_capital −= capital`, préstamo `PAGADO` si era la última | ✅ |
| RFC-23 | Rechazar pagos mayores a la deuda | U2 |
| RFC-24 | Cancelación anticipada sin intereses futuros | U2 |

> **Nota regulatoria (Perú, SBS):** un pago mayor a dos cuotas (incluida la exigible) cuenta como *pago anticipado* y debe reducir intereses. Uno menor cuenta como *adelanto de cuotas*.

#### H. Mora (Unidad 2)
| ID | Requerimiento |
|---|---|
| RFC-25 | Proceso de cartera: cuotas `PENDIENTE` con `fecha_vencimiento < hoy` → `VENCIDA` (al iniciar la aplicación o desde un botón del admin) |
| RFC-26 | Interés moratorio: `mora = pendiente × TMD × días`. **Se recalcula y sobrescribe; no se acumula** (idempotente). |
| RFC-27 | Préstamo con cuotas vencidas → `EN_MORA` |
| RFC-28 | Días de gracia (opcional) |

> En Perú, la Ley 31143 (2021) limitó el cobro por incumplimiento al interés moratorio; no se permiten penalidades fijas.

#### I. Administración (Unidad 2)
| ID | Requerimiento |
|---|---|
| RFC-29 | Crear, editar y desactivar productos (sin cambiar la tasa si ya tienen préstamos) |
| RFC-30 | Bandeja de solicitudes pendientes |
| RFC-31 | Cartera e índice de morosidad = Σ saldo_capital `EN_MORA` / Σ saldo_capital (`ACTIVO` + `EN_MORA`) |
| RFC-32 | Ejecutar el proceso de mora y fijar la fecha simulada |

### 5.6 Fórmulas

| Concepto | Fórmula |
|---|---|
| TEM | `TEM = (1 + TEA)^(1/12) − 1` |
| Cuota fija (método francés) | `C = P × TEM / (1 − (1 + TEM)^−n)` |
| Cuota k | `interés_k = saldo × TEM` ; `capital_k = C − interés_k` ; `saldo −= capital_k` |
| Última cuota | `capital_n = saldo restante` |
| TMD (U2) | `TMD = (1 + tasa_moratoria_anual)^(1/360) − 1` |
| TCEA (U2) | `i` tal que `P_recibido = Σ C_k / (1+i)^k`; `TCEA = (1+i)^12 − 1` |

- Los montos se redondean a 2 decimales con `RoundingMode.HALF_EVEN`. La TEM **no** se redondea (`MathContext.DECIMAL64`).
- **Unidad 1:** no hay comisiones ni seguros, así que `TCEA = TEA`. Se guarda la TEA en `prestamo.tcea` y se documenta la decisión.
- **Opción futura:** una comisión de desembolso (`monto_desembolsado = monto_solicitado − comisión`) haría que la TCEA sea mayor que la TEA.

**Caso de prueba de referencia:** P = S/ 1 000, TEA = 40 %, n = 6 → TEM = 2.8436 %, cuota = **S/ 183.64**.

| N° | Cuota | Interés | Capital | Saldo |
|---|---|---|---|---|
| 1 | 183.64 | 28.44 | 155.20 | 844.80 |
| 2 | 183.64 | 24.02 | 159.62 | 685.18 |
| 3 | 183.64 | 19.48 | 164.16 | 521.02 |
| 4 | 183.64 | 14.82 | 168.82 | 352.20 |
| 5 | 183.64 | 10.02 | 173.62 | 178.58 |
| 6 | **183.66** | 5.08 | 178.58 | 0.00 |

Total de intereses: **S/ 101.86**.

### 5.7 Modelo de score (Unidad 2)

| Factor | Fuente | Puntos |
|---|---|---|
| Antigüedad de la cuenta | `cliente.fecha_registro` | < 30 días: 0 · 30–180 días: 100 · > 180 días: 200 |
| Actividad (movimientos en 90 días) | `movimiento` | 0: 0 · 1–10: 75 · > 10: 150 |
| Ingresos (abonos `+` sin contar desembolsos) | `movimiento` ⋈ `transaccion` | Hasta 200 según la relación ingreso/cuota |
| Saldo promedio | `movimiento.saldo_posterior` | Hasta 100 |
| Préstamos pagados o cancelados | `prestamo` | +75 cada uno, máximo 200 |
| Puntualidad | `pago.fecha` vs `cuota.fecha_vencimiento` | % puntual × 150 |
| Cuotas pagadas con mora | `pago_detalle.aplicado_mora > 0` | −50 cada una, hasta −200 |

Un cliente nuevo obtiene entre 0 y 100 puntos. El microcrédito es su puerta de entrada para construir historial.

---

## 6. Domain Services

### 6.1 Criterio

| Pregunta | Si la respuesta es sí, va en… |
|---|---|
| ¿La regla usa solo los datos de una entidad? | **La entidad** |
| ¿Es un valor con validación propia? | **Value object** |
| ¿Cruza varias entidades o agregados, o es un cálculo o política sin dueño, **y no necesita BD ni recursos externos**? | **Domain Service** |
| ¿Necesita BD, fecha del sistema, hashing, transacciones u orquestación? | **Application Service** + puertos |

**Reglas de oro:**
1. Sin estado.
2. No conoce repositorios ni puertos; el caso de uso le entrega los datos ya cargados.
3. No lee la fecha actual por su cuenta; recibe `hoy` o `ahora` como parámetro.

### 6.2 Domain Services de la Unidad 1 (se implementan 3)

#### ✅ `ServicioTransferencia` (billetera)

| Aspecto | Detalle |
|---|---|
| Recibe | Billetera de origen, billetera de destino, monto, tipo de transacción, fecha y hora |
| Valida | Que no sean la misma billetera; ambas `ACTIVA`, monto > 0 y saldo suficiente (vía `debitar()` / `acreditar()`) |
| Devuelve | `Result<Transaccion>` con dos movimientos (− origen, + destino) y el `saldo_posterior` de cada uno |
| No hace | Buscar por celular, bloquear filas ni guardar (eso es del `TransferirUseCase`) |
| Por qué es Domain Service | Modifica **dos** agregados `Billetera` |
| Tests | Transferencia correcta, saldo insuficiente, misma billetera, origen bloqueado, destino bloqueado |

#### ✅ `CalculadoraCronograma` (crédito)

| Aspecto | Detalle |
|---|---|
| Recibe | Monto, TEA, plazo y fecha de desembolso |
| Devuelve | `Cronograma` (value object): TEM, cuota fija, total de intereses, total a pagar y `List<CuotaProyectada>` |
| Reglas | Método francés, redondeo `HALF_EVEN`; la última cuota absorbe la diferencia |
| La usan | `SimularCreditoUseCase` y `SolicitarCreditoUseCase` (mismo código, así que el cronograma simulado y el real son idénticos) |
| Test | La tabla de la sección 5.6 |

#### ✅ `EvaluadorCrediticio` (crédito, versión simple)

| Aspecto | Detalle |
|---|---|
| Recibe | Producto, monto, plazo y `PerfilCrediticio` (record: billetera activa, tiene préstamo activo del mismo tipo, tiene préstamo en mora, cantidad de microcréditos pagados) |
| Devuelve | `Result<Void>`: aprobado, o fallo con el código de `motivo_rechazo` |
| Pospuesto | Score, patrón Strategy y revisión manual |
| Tests | Un test por cada motivo de rechazo, más el caso aprobado |

### 6.3 Lo que va en las entidades (Unidad 1)

| Lógica | Dónde |
|---|---|
| Retiro | `billetera.debitar(monto)` |
| Pagar la próxima cuota | `prestamo.pagarSiguienteCuota()` (agregado `Prestamo` con su `List<Cuota>`) |
| Transiciones | `solicitud.aprobar()`, `solicitud.rechazar(motivo)`, `prestamo.marcarPagado()` |
| Validar DNI y celular | Método de fábrica de `Cliente` |
| Validar monto y plazo | `producto.validarCondiciones(monto, plazo)` |

### 6.4 Domain Services pospuestos a la Unidad 2

| Domain Service | Propósito |
|---|---|
| `CalculadoraMora` | Interés moratorio idempotente (RFC-18, RFC-24 a RFC-27) |
| `CalculadoraTCEA` | TIR por bisección o Newton-Raphson |
| `PoliticaCrediticia` + `PoliticaMicrocredito` / `PoliticaCreditoPersonal` | Patrón Strategy por tipo de producto; score y revisión manual |
| `PoliticaLimitesOperacion` | Límites diarios (RF-23) |
| (`AplicadorDePagos`) | Solo si las cuotas no se cargan dentro de `Prestamo` |

### 6.5 Lo que parece Domain Service pero es un puerto

| Tema | Puerto |
|---|---|
| Hashear o validar contraseñas | `PasswordHasher` |
| Fecha actual | `Clock` |
| Historial del cliente | `HistorialCrediticioQuery` |
| Números de operación con secuencias de BD | Infraestructura |

Las reglas de fortaleza de contraseña sí son dominio (value object `PasswordPlano`).

### 6.6 Cómo encajan con los casos de uso

**`SolicitarCreditoUseCase`:**
1. Cargar el cliente, la billetera y el producto (repositorios).
2. Armar el `PerfilCrediticio` (puerto de consulta).
3. `evaluadorCrediticio.evaluar(...)` → **Domain Service**.
4. Si se rechaza: `solicitud.rechazar(motivo)`.
5. Si se aprueba: `solicitud.aprobar()`, luego `calculadoraCronograma.generar(...)` → **Domain Service**, crear el `Prestamo` con sus cuotas y acreditar la billetera.
6. Guardar todo en `transactionManager.inTransaction(...)`.

**`TransferirUseCase`:**
1. Buscar la billetera de destino por celular.
2. Bloquear ambas billeteras en orden de id (`findByIdForUpdate`).
3. `servicioTransferencia.transferir(...)` → **Domain Service**.
4. Guardar la transacción, los movimientos y los saldos en la transacción de BD.

---

## 7. Decisiones del equipo: sin recargas y retiro

### 7.1 Sin recargas

El equipo decidió **no implementar recargas**. El cliente obtiene saldo solo por transferencias (y por los desembolsos de crédito).

**Consecuencia importante:** todas las billeteras empiezan en S/ 0 y las transferencias solo mueven dinero; no lo crean.

| Operación | Efecto sobre el dinero total del sistema |
|---|---|
| Transferencia | Ninguno |
| Desembolso de crédito | Entra |
| Pago de cuota | Sale (capital + interés) |
| Retiro | Sale |

Sin una fuente inicial de dinero no se pueden probar ni presentar las transferencias. Además, como los clientes devuelven más de lo que reciben por los intereses, el sistema en conjunto no podría pagar todas las deudas.

**Solución adoptada: carga inicial por script (no es una funcionalidad del sistema).**

| Opción | Evaluación |
|---|---|
| **A. Carga inicial por script SQL** ✅ | Por cada usuario de demo: `transaccion` de tipo `CARGA_INICIAL` + `movimiento` (+) con `saldo_posterior` + actualizar `billetera.saldo`. El historial queda consistente, respeta la decisión del equipo y no requiere código Java. |
| B. Bono de bienvenida al registrarse | Realista, pero crear cuentas regala dinero y es más código. |
| C. `UPDATE billetera SET saldo = ...` directo | ❌ El saldo dejaría de coincidir con los movimientos. |

### 7.2 Retiro

**Qué es:** lo opuesto a una recarga. Sirve para sacar el dinero de la billetera hacia el mundo real.

| Modalidad real | Cómo funciona |
|---|---|
| A una cuenta bancaria | El cliente ingresa el banco y su CCI (20 dígitos); la billetera envía una transferencia interbancaria. |
| En un agente o cajero | Se genera un código temporal que el cliente canjea por efectivo. |

En ambos casos, **del lado del sistema** se debita la billetera y se registra una `transaccion` de tipo `RETIRO` y un `movimiento` (−). Lo externo siempre es simulado en un proyecto académico.

**Coherencia:** el argumento para descartar las recargas ("no hay forma real") aplica igual al retiro. Lo coherente es simular ambas o ninguna.

**Recomendación: "Retiro a cuenta bancaria" simulado.**
- En código es la mitad de una transferencia (1 o 2 horas).
- Es una función básica de una billetera (sacar tu dinero).
- **Formulario:** banco (combo), número de cuenta (validar solo el formato) y monto → confirmación → comprobante.
- **Reglas:** monto > 0, saldo suficiente, billetera `ACTIVA` y, opcionalmente, un mínimo de S/ 10.
- **Limitación:** el esquema no guarda el banco ni la cuenta; en la Unidad 1 solo se muestran en el comprobante.

Si el equipo decide no implementarlo, debe presentarse como una decisión de alcance por coherencia con la ausencia de recargas.

---

## 8. Alcance final de la Unidad 1

### ✅ Billetera

| Funcionalidad | RF |
|---|---|
| Registro (usuario + cliente + billetera en una transacción de BD) | RF-01, 09, 10, 13 |
| Login y logout | RF-02, 03, 04 |
| Ver saldo | RF-14 |
| Carga inicial de saldo por script (no es funcionalidad) | — |
| Retiro a cuenta bancaria (simulado) | RF-19 |
| Transferir por celular con el nombre del destinatario enmascarado | RF-20, 21, 22 |
| Historial de movimientos | RF-29 |
| Reglas: saldo ≥ 0, billetera bloqueada no opera, atomicidad | RF-16, 17, 25, 26 |

### ✅ Crédito mínimo funcional

| Funcionalidad | Simplificación |
|---|---|
| Listar productos | Vienen del seed; sin mantenimiento |
| Simular con cronograma | Método francés completo |
| Solicitar | Evaluación automática para ambos tipos con filtros simples, sin score. Si se aprueba, se desembolsa en el mismo paso. |
| Mis préstamos y cronograma | — |
| Pagar la próxima cuota | Solo la cuota completa |

### Qué se mantiene del diseño y qué se simplifica

| Pieza | Decisión |
|---|---|
| `TransactionManager` (puerto + ThreadLocal) | **Se mantiene.** Garantiza la atomicidad (unas 40 líneas). |
| `SELECT ... FOR UPDATE` en orden de id | **Se mantiene** |
| `CalculadoraCronograma`, `ServicioTransferencia` | **Se mantienen** |
| `EvaluadorCrediticio` | **Simplificado** (filtros; `score_obtenido = NULL`) |
| PBKDF2 | **Se mantiene** |
| Value objects `Dni` / `Celular` | Opcionales |
| `CalculadoraMora`, `CalculadoraTCEA` | Pospuestos |
| Cambios al esquema | Solo lo mínimo: ordenar el script, datos semilla e índice en `movimiento(billetera_id)` |

### Datos semilla

| Tabla | Filas |
|---|---|
| `seguridad.rol` | `CLIENTE` (y `ADMIN` para el futuro) |
| `billetera.tipo_transaccion` | `CARGA_INICIAL` (CREDITO), `TRANSFERENCIA`, `RETIRO` (DEBITO), `DESEMBOLSO_CREDITO` (CREDITO), `PAGO_CREDITO` (DEBITO) |
| `credito.producto_crediticio` | 1 microcrédito y 1 o 2 créditos personales |
| Usuarios de demo | 2 o 3 usuarios con su `CARGA_INICIAL` (por ejemplo S/ 1 000) |

---

## 9. Casos de uso de la Unidad 1

### Seguridad
| Caso de uso | Entrada → Salida | Puertos de salida |
|---|---|---|
| `RegistrarClienteUseCase` | correo, contraseña, nombres, apellidos, DNI, celular → id del cliente | UsuarioRepo, ClienteRepo, BilleteraRepo, RolRepo, PasswordHasher, TransactionManager |
| `IniciarSesionUseCase` | correo, contraseña → sesión (usuarioId, clienteId, nombre) | UsuarioRepo, ClienteRepo, PasswordHasher |

### Billetera
| Caso de uso | Entrada → Salida | Puertos / Domain Service |
|---|---|---|
| `ConsultarBilleteraUseCase` | clienteId → saldo y estado | BilleteraRepo |
| `RetirarSaldoUseCase` 🟡 | clienteId, banco, cuenta, monto → comprobante | BilleteraRepo, TransaccionRepo, TransactionManager |
| `BuscarDestinatarioUseCase` | celular → nombre enmascarado | ClienteRepo |
| `TransferirUseCase` | clienteId origen, celular destino, monto → comprobante | BilleteraRepo (`ForUpdate`), TransaccionRepo, TransactionManager, **ServicioTransferencia** |
| `ConsultarMovimientosUseCase` | clienteId → movimientos | MovimientoQuery |

### Crédito
| Caso de uso | Entrada → Salida | Puertos / Domain Service |
|---|---|---|
| `ListarProductosUseCase` | — → productos | ProductoRepo |
| `SimularCreditoUseCase` | productoId, monto, plazo → cronograma | ProductoRepo, **CalculadoraCronograma** |
| `SolicitarCreditoUseCase` | clienteId, productoId, monto, plazo → aprobada con préstamo o rechazada con motivo | ProductoRepo, SolicitudRepo, PrestamoRepo, BilleteraRepo, TransaccionRepo, TransactionManager, **EvaluadorCrediticio**, **CalculadoraCronograma** |
| `ConsultarPrestamosUseCase` | clienteId → préstamos | PrestamoRepo |
| `ConsultarCronogramaUseCase` | prestamoId → cuotas | PrestamoRepo |
| `PagarCuotaUseCase` | clienteId, prestamoId → comprobante | PrestamoRepo, PagoRepo, BilleteraRepo, TransaccionRepo, TransactionManager |

**Filtros del `EvaluadorCrediticio` (U1):** monto y plazo en rango · billetera `ACTIVA` · sin préstamo `ACTIVO` del mismo tipo · sin préstamo `EN_MORA` · el crédito personal exige al menos un microcrédito `PAGADO`.

**`PagarCuotaUseCase` (U1):** toma la primera cuota no pagada, cobra `capital + interés`, crea el `pago` y un `pago_detalle`, marca la cuota `PAGADA` y hace `saldo_capital −= capital`. Si era la última cuota, el préstamo pasa a `PAGADO`.

---

## 10. Plan de 5 días

### Día 1: base y acceso
- [ ] **Primer commit** (hoy `domain/`, `application/` e `infrastructure/` están fuera de git). Un commit al final de cada día.
- [ ] Script SQL ordenado + datos semilla (sección 8).
- [ ] `pom.xml`: driver de PostgreSQL. `config.properties` fuera de git.
- [ ] Conexión + `JdbcTransactionManager`.
- [ ] Corregir `RegistrarUsuarioUseCase` y sus comandos, y el constructor de `Billetera`.
- [ ] Hasher PBKDF2.
- [ ] Casos de uso de Registro y Login + adaptadores JDBC + pantallas de Login y Registro.
- [ ] `Main` como composition root.

**Terminado cuando:** puedes registrarte, cerrar la aplicación, volver a abrirla e iniciar sesión.

### Día 2: billetera
- [ ] `Billetera.acreditar()` / `debitar()` con validación de estado y saldo.
- [ ] Script de carga inicial de saldo para los usuarios de demo.
- [ ] Consultar saldo, consultar movimientos y retiro simulado.
- [ ] Dashboard: saldo, botones de acción y `JTable` de movimientos.
- [ ] Diálogo de retiro.
- [ ] Si sobra tiempo: empezar con las transferencias.

**Terminado cuando:** los usuarios de demo ven su saldo e historial y pueden retirar, y todo cuadra con la BD.

### Día 3: transferencias
- [ ] `ServicioTransferencia` + sus tests.
- [ ] `findByIdForUpdate` con bloqueo en orden de id.
- [ ] Buscar destinatario + transferir.
- [ ] Diálogo: celular → "¿Enviar S/ X a Juan P***?" → comprobante.
- [ ] Casos de error: a uno mismo, saldo insuficiente, celular inexistente, billetera bloqueada (bloquear a mano en la BD).
- [ ] Prueba de rollback: provocar un error a mitad de la transferencia y verificar que el saldo no cambió.

**Terminado cuando:** la billetera completa funciona entre dos usuarios.

### Día 4: crédito (simular y solicitar)
- [ ] `CalculadoraCronograma` + **test** con la tabla de la sección 5.6.
- [ ] Listar productos, simular.
- [ ] `EvaluadorCrediticio` + tests.
- [ ] Solicitar crédito (aprueba y desembolsa en una sola transacción de BD).
- [ ] Pantalla de créditos: combo de productos, monto, plazo, tabla de cronograma y botones Simular y Solicitar.

**Terminado cuando:** solicitas un microcrédito, el saldo sube y el cronograma aparece en la BD.

### Día 5: pagos y entrega
- [ ] **Mañana:** Mis préstamos, cronograma y pagar la próxima cuota, con su pantalla.
- [ ] **Tarde:**
  - Prueba completa: registro → transferencia → microcrédito → pagar todas las cuotas → se habilita el crédito personal.
  - Datos de demo con historial.
  - Diagrama de arquitectura (hexágono con los puertos).
  - README.
- [ ] Si sobra tiempo: al iniciar la aplicación, marcar como `VENCIDA` las cuotas con `fecha_vencimiento < hoy` (sin calcular mora).

---

## 11. Pautas de Swing

- Una sola ventana principal con **`CardLayout`** (Login ↔ Registro ↔ Dashboard ↔ Créditos) y `JDialog` para las operaciones.
- La sesión (usuarioId, clienteId, nombre) va en `SesionActual` dentro de `infrastructure/adapter/in/swing`. **No es dominio.**
- Los listeners solo leen los campos, llaman al use case y muestran el `Result` (`JOptionPane` si falla, refrescar la pantalla si tiene éxito). **Ninguna regla de negocio en la vista.**
- Tablas: `DefaultTableModel` a partir de la lista que devuelve el caso de uso.
- `SwingWorker` solo si la interfaz se congela.

---

## 12. Preparación para la Unidad 2 (Spring)

| Hoy (Unidad 1) | En Spring (Unidad 2) |
|---|---|
| `domain/` | **Se reutiliza sin cambios** |
| `application/` (use cases, records) | **Casi sin cambios**; los records pasan a ser DTOs |
| Pantallas Swing | `@RestController` que llaman a los mismos use cases |
| Adaptadores JDBC | JPA o `JdbcTemplate` implementando los mismos puertos |
| `TransactionManager` propio | `@Transactional` |
| `Main` como composition root | Beans de configuración de Spring |

**Regla que no se recorta:** `domain` y `application` no deben tener ninguna referencia a `javax.swing` ni a `java.sql`.

---

## 13. Riesgos

1. **Diseñar demasiado.** Si una clase no aparece en este plan, va a la Unidad 2.
2. **Atascarse en Swing.** Límite de tiempo por pantalla; primero que funcione, después que se vea bien.
3. **Redondeo del cronograma.** El test va antes que la pantalla.
4. **Olvidar el rollback.** Probarlo a propósito.
5. **Sin commits.** Uno al final de cada día como mínimo.
6. **Sin saldo inicial.** Sin el script de carga inicial no hay demo.

---

## 14. Backlog para las Unidades 2 y 3

- Rol ADMIN: bandeja de solicitudes, mantenimiento de productos, gestión de usuarios y billeteras.
- Modelo de score + patrón Strategy por tipo de producto (revisión manual del crédito personal).
- Mora: `CalculadoraMora`, puerto `Clock` con fecha simulada, estados `VENCIDA` / `EN_MORA`.
- Pagos parciales, monto libre y cancelación anticipada (`CANCELADO`).
- TCEA real con TIR; comisión de desembolso.
- Límites diarios y comisiones por operación.
- Editar el perfil, cambiar la contraseña, bloqueo por intentos fallidos.
- Filtros del historial, detalle de transacción y exportar estado de cuenta.
- Cambios al esquema de la sección 3.
- Notificaciones y auditoría.
