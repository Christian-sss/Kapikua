package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.dto.response.ComprobanteFilaResponse;
import com.projects.application.port.out.ComprobanteQuery;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class PgComprobanteQueryAdapter extends BaseRepository implements ComprobanteQuery {

    private static final String BUSCAR_POR_TRANSACCION = """
            SELECT t.id AS transaccion_id, t.fecha, tt.codigo AS tipo_codigo, t.monto AS monto_transaccion,
                   t.estado, t.referencia, c.id AS cliente_id, c.nombres, c.apellidos, m.signo, m.saldo_posterior
            FROM billetera.movimiento m
            JOIN billetera.transaccion t ON t.id = m.transaccion_id
            JOIN billetera.tipo_transaccion tt ON tt.id = t.tipo_id
            JOIN billetera.billetera b ON b.id = m.billetera_id
            JOIN billetera.cliente c ON c.id = b.cliente_id
            WHERE t.id = ?
            """;

    @Override
    public List<ComprobanteFilaResponse> buscarPorTransaccionId(Long transaccionId) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(BUSCAR_POR_TRANSACCION)) {
                stmt.setLong(1, transaccionId);

                try (var rs = stmt.executeQuery()) {
                    List<ComprobanteFilaResponse> resultado = new ArrayList<>();
                    while (rs.next()) {
                        resultado.add(new ComprobanteFilaResponse(
                                rs.getLong("transaccion_id"),
                                rs.getObject("fecha", OffsetDateTime.class),
                                rs.getString("tipo_codigo"),
                                rs.getBigDecimal("monto_transaccion"),
                                rs.getString("estado"),
                                rs.getString("referencia"),
                                rs.getLong("cliente_id"),
                                rs.getString("nombres"),
                                rs.getString("apellidos"),
                                rs.getString("signo").charAt(0),
                                rs.getBigDecimal("saldo_posterior")
                        ));
                    }
                    return resultado;
                }
            }
        });
    }
}
