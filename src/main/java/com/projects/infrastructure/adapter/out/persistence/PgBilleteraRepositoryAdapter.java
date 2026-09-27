package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.BilleteraRepository;
import com.projects.domain.model.EstadoBilletera;
import com.projects.domain.model.billetera.Billetera;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.Optional;

public class PgBilleteraRepositoryAdapter extends BaseRepository implements BilleteraRepository {


    private static final String SAVE_BILLETERA = """

            INSERT INTO billetera.billetera (cliente_id, saldo, estado)
            VALUES ( ?,?,? );

            """;

    private static final String FIND_BY_CLIENTE_ID = """
            SELECT id, cliente_id, saldo, estado, fecha_creacion
            FROM billetera.billetera
            WHERE cliente_id = ?
            """;

    private static final String FIND_BY_ID_PARA_ACTUALIZAR = """
            SELECT id, cliente_id, saldo, estado, fecha_creacion
            FROM billetera.billetera
            WHERE id = ?
            FOR UPDATE
            """;

    private static final String ACTUALIZAR_SALDO = """
            UPDATE billetera.billetera
            SET saldo = ?
            WHERE id = ?
            """;


    @Override
    public Optional<Billetera> save(Billetera billetera) {

        return ejecutar(conn -> {

            try(var stmt = conn.prepareStatement(SAVE_BILLETERA, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setLong(1,billetera.getClienteId());
                stmt.setBigDecimal(2,billetera.getSaldo());
                stmt.setString(3,billetera.getEstadoBilletera().name());


                int filaAfectada = stmt.executeUpdate();

                if(filaAfectada == 0) {
                    return Optional.empty();
                }


                try (var rs = stmt.getGeneratedKeys()) {

                    if(rs.next()) {

                        billetera.setId(rs.getLong("id"));
                        return Optional.of(billetera);

                    }

                    return Optional.empty();

                }
            }
        });

    }

    @Override
    public Optional<Billetera> findByClienteId(Long clienteId) {
        return ejecutar(conn -> buscarPorId(conn, FIND_BY_CLIENTE_ID, clienteId));
    }

    @Override
    public Optional<Billetera> findByIdParaActualizar(Long id) {
        return ejecutar(conn -> buscarPorId(conn, FIND_BY_ID_PARA_ACTUALIZAR, id));
    }

    private Optional<Billetera> buscarPorId(Connection conn, String sql, Long valor) throws SQLException {
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, valor);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(Billetera.reconstruir(
                            rs.getLong("id"),
                            rs.getLong("cliente_id"),
                            rs.getBigDecimal("saldo"),
                            EstadoBilletera.valueOf(rs.getString("estado")),
                            rs.getObject("fecha_creacion", OffsetDateTime.class)
                    ));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public void actualizarSaldo(Billetera billetera) {
        ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(ACTUALIZAR_SALDO)) {
                stmt.setBigDecimal(1, billetera.getSaldo());
                stmt.setLong(2, billetera.getId());
                stmt.executeUpdate();
                return null;
            }
        });
    }
}
