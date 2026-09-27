package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.ProductoRepository;
import com.projects.domain.model.TipoProductoCrediticio;
import com.projects.domain.model.credito.ProductoCrediticio;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PgProductoRepositoryAdapter extends BaseRepository implements ProductoRepository {

    private static final String COLUMNAS = """
            SELECT id, nombre, tipo, monto_minimo, monto_maximo,
                   plazo_minimo_meses, plazo_maximo_meses, tasa_interes_anual
            FROM credito.producto_crediticio
            """;

    private static final String LISTAR = COLUMNAS + " ORDER BY tipo DESC, monto_minimo";

    private static final String FIND_BY_ID = COLUMNAS + " WHERE id = ?";

    @Override
    public List<ProductoCrediticio> listar() {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(LISTAR);
                 var rs = stmt.executeQuery()) {
                List<ProductoCrediticio> productos = new ArrayList<>();
                while (rs.next()) {
                    productos.add(mapear(rs));
                }
                return productos;
            }
        });
    }

    @Override
    public Optional<ProductoCrediticio> findById(Long id) {
        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(FIND_BY_ID)) {
                stmt.setLong(1, id);
                try (var rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
                }
            }
        });
    }

    private ProductoCrediticio mapear(ResultSet rs) throws SQLException {
        return new ProductoCrediticio(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getBigDecimal("monto_minimo"),
                rs.getBigDecimal("monto_maximo"),
                TipoProductoCrediticio.valueOf(rs.getString("tipo")),
                rs.getInt("plazo_minimo_meses"),
                rs.getInt("plazo_maximo_meses"),
                rs.getBigDecimal("tasa_interes_anual")
        );
    }
}
