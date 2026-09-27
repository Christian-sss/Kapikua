package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.dto.response.CarteraResponse;
import com.projects.application.dto.response.ResumenTransaccionesResponse;
import com.projects.application.dto.response.TransaccionAdminResponse;
import com.projects.application.dto.response.VolumenTipoResponse;
import com.projects.application.port.out.EstadisticasQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PgEstadisticasQueryAdapter extends BaseRepository implements EstadisticasQuery {

    private static final String RESUMEN = """
            SELECT COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE estado = 'EXITOSA') AS exitosas,
                   COALESCE(SUM(monto) FILTER (WHERE estado = 'EXITOSA'), 0) AS volumen
            FROM billetera.transaccion
            WHERE fecha >= ?
            """;

    // date_trunc sobre timestamptz usa la zona horaria de la sesión (la de la JVM con pgjdbc).
    private static final String VOLUMEN_AGRUPADO = """
            SELECT date_trunc('%s', fecha)::date AS inicio, SUM(monto) AS volumen
            FROM billetera.transaccion
            WHERE estado = 'EXITOSA' AND fecha >= ?
            GROUP BY 1
            """;

    private static final String VOLUMEN_POR_TIPO = """
            SELECT tt.codigo, COUNT(*) AS cantidad, SUM(t.monto) AS monto
            FROM billetera.transaccion t
            JOIN billetera.tipo_transaccion tt ON tt.id = t.tipo_id
            WHERE t.estado = 'EXITOSA' AND t.fecha >= ?
            GROUP BY tt.codigo
            ORDER BY monto DESC
            """;

    private static final String CARTERA = """
            SELECT COALESCE(SUM(saldo_capital) FILTER (WHERE estado IN ('ACTIVO', 'EN_MORA')), 0) AS vigente,
                   COALESCE(SUM(saldo_capital) FILTER (WHERE estado = 'EN_MORA'), 0) AS en_mora,
                   COUNT(*) FILTER (WHERE estado IN ('ACTIVO', 'EN_MORA')) AS prestamos_vigentes,
                   COUNT(*) FILTER (WHERE estado = 'EN_MORA') AS prestamos_en_mora
            FROM credito.prestamo
            """;

    private static final String ULTIMAS_TRANSACCIONES = """
            SELECT t.id, t.fecha, tt.codigo, t.monto, t.estado,
                   COALESCE(origen.numero_celular, 'KAPIKUA') AS origen,
                   COALESCE(destino.numero_celular, t.referencia, 'KAPIKUA') AS destino
            FROM billetera.transaccion t
            JOIN billetera.tipo_transaccion tt ON tt.id = t.tipo_id
            LEFT JOIN LATERAL (
                SELECT c.numero_celular
                FROM billetera.movimiento m
                JOIN billetera.billetera b ON b.id = m.billetera_id
                JOIN billetera.cliente c ON c.id = b.cliente_id
                WHERE m.transaccion_id = t.id AND m.signo = '-'
                LIMIT 1
            ) origen ON TRUE
            LEFT JOIN LATERAL (
                SELECT c.numero_celular
                FROM billetera.movimiento m
                JOIN billetera.billetera b ON b.id = m.billetera_id
                JOIN billetera.cliente c ON c.id = b.cliente_id
                WHERE m.transaccion_id = t.id AND m.signo = '+'
                LIMIT 1
            ) destino ON TRUE
            ORDER BY t.fecha DESC
            LIMIT ?
            """;

    @Override
    public ResumenTransaccionesResponse resumenTransacciones(OffsetDateTime desde) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(RESUMEN)) {
                stmt.setObject(1, desde);
                try (var rs = stmt.executeQuery()) {
                    rs.next();
                    return new ResumenTransaccionesResponse(
                            rs.getLong("total"),
                            rs.getLong("exitosas"),
                            rs.getBigDecimal("volumen")
                    );
                }
            }
        });
    }

    @Override
    public Map<LocalDate, BigDecimal> volumenPorDia(OffsetDateTime desde) {
        return volumenAgrupado("day", desde);
    }

    @Override
    public Map<LocalDate, BigDecimal> volumenPorMes(OffsetDateTime desde) {
        return volumenAgrupado("month", desde);
    }

    private Map<LocalDate, BigDecimal> volumenAgrupado(String unidad, OffsetDateTime desde) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(VOLUMEN_AGRUPADO.formatted(unidad))) {
                stmt.setObject(1, desde);
                try (var rs = stmt.executeQuery()) {
                    Map<LocalDate, BigDecimal> volumen = new HashMap<>();
                    while (rs.next()) {
                        volumen.put(rs.getObject("inicio", LocalDate.class), rs.getBigDecimal("volumen"));
                    }
                    return volumen;
                }
            }
        });
    }

    @Override
    public List<VolumenTipoResponse> volumenPorTipo(OffsetDateTime desde) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(VOLUMEN_POR_TIPO)) {
                stmt.setObject(1, desde);
                try (var rs = stmt.executeQuery()) {
                    List<VolumenTipoResponse> tipos = new ArrayList<>();
                    while (rs.next()) {
                        tipos.add(new VolumenTipoResponse(
                                rs.getString("codigo"),
                                rs.getLong("cantidad"),
                                rs.getBigDecimal("monto")
                        ));
                    }
                    return tipos;
                }
            }
        });
    }

    @Override
    public CarteraResponse resumenCartera() {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(CARTERA);
                 var rs = stmt.executeQuery()) {
                rs.next();
                return new CarteraResponse(
                        rs.getBigDecimal("vigente"),
                        rs.getBigDecimal("en_mora"),
                        rs.getLong("prestamos_vigentes"),
                        rs.getLong("prestamos_en_mora")
                );
            }
        });
    }

    @Override
    public List<TransaccionAdminResponse> ultimasTransacciones(int limite) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(ULTIMAS_TRANSACCIONES)) {
                stmt.setInt(1, limite);
                try (var rs = stmt.executeQuery()) {
                    List<TransaccionAdminResponse> transacciones = new ArrayList<>();
                    while (rs.next()) {
                        transacciones.add(new TransaccionAdminResponse(
                                rs.getLong("id"),
                                rs.getObject("fecha", OffsetDateTime.class),
                                rs.getString("origen"),
                                rs.getString("destino"),
                                rs.getString("codigo"),
                                rs.getBigDecimal("monto"),
                                rs.getString("estado")
                        ));
                    }
                    return transacciones;
                }
            }
        });
    }
}
