package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.HistorialCrediticioQuery;
import com.projects.domain.model.EstadoPrestamo;
import com.projects.domain.model.TipoProductoCrediticio;

public class PgHistorialCrediticioQueryAdapter extends BaseRepository implements HistorialCrediticioQuery {

    private static final String EXISTE_EN_MORA = """
            SELECT EXISTS (
                SELECT 1
                FROM credito.prestamo p
                JOIN credito.solicitud_credito s ON s.id = p.solicitud_id
                WHERE s.cliente_id = ? AND p.estado = 'EN_MORA'
            )
            """;

    private static final String EXISTE_POR_TIPO_Y_ESTADO = """
            SELECT EXISTS (
                SELECT 1
                FROM credito.prestamo p
                JOIN credito.solicitud_credito s ON s.id = p.solicitud_id
                JOIN credito.producto_crediticio pc ON pc.id = s.producto_id
                WHERE s.cliente_id = ? AND pc.tipo = ? AND p.estado = ?
            )
            """;

    @Override
    public boolean tienePrestamoEnMora(Long clienteId) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(EXISTE_EN_MORA)) {
                stmt.setLong(1, clienteId);
                try (var rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getBoolean(1);
                }
            }
        });
    }

    @Override
    public boolean tienePrestamoActivo(Long clienteId, TipoProductoCrediticio tipo) {
        return existe(clienteId, tipo, EstadoPrestamo.ACTIVO);
    }

    @Override
    public boolean tienePrestamoPagado(Long clienteId, TipoProductoCrediticio tipo) {
        return existe(clienteId, tipo, EstadoPrestamo.PAGADO);
    }

    private boolean existe(Long clienteId, TipoProductoCrediticio tipo, EstadoPrestamo estado) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(EXISTE_POR_TIPO_Y_ESTADO)) {
                stmt.setLong(1, clienteId);
                stmt.setString(2, tipo.name());
                stmt.setString(3, estado.name());
                try (var rs = stmt.executeQuery()) {
                    rs.next();
                    return rs.getBoolean(1);
                }
            }
        });
    }
}
