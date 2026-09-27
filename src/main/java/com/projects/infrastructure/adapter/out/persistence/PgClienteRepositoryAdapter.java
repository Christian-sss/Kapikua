package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.ClienteRepository;
import com.projects.domain.model.billetera.Cliente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.Optional;

public class PgClienteRepositoryAdapter extends BaseRepository implements ClienteRepository {

    private static final String EXISTS_DNI= """
            SELECT 1
            FROM billetera.cliente c
            WHERE c.dni = ?
    """;


    private static final String EXISTS_CELULAR = """
            SELECT 1
            FROM billetera.cliente c
            WHERE c.numero_celular = ?
    """;

    private static final String CAMPOS_CLIENTE =
            "id, usuario_id, nombres, apellidos, dni, numero_celular, fecha_nacimiento, fecha_registro";

    private static final String FIND_BY_CELULAR = """
            SELECT %s
            FROM billetera.cliente
            WHERE numero_celular = ?
            """.formatted(CAMPOS_CLIENTE);

    private static final String FIND_BY_USUARIO_ID = """
            SELECT %s
            FROM billetera.cliente
            WHERE usuario_id = ?
            """.formatted(CAMPOS_CLIENTE);

    private static final String SAVE_CLIENTE = """

            INSERT INTO billetera.cliente (usuario_id, nombres, apellidos, dni, numero_celular, fecha_nacimiento)
            VALUES (?,?,?,?,?,?);

    """;



    @Override
    public Optional<Cliente> save(Cliente cliente) {

        return ejecutar(conn -> {

            try(var stmt = conn.prepareStatement(SAVE_CLIENTE, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1,cliente.getUsuarioId());
                stmt.setString(2,cliente.getNombres());
                stmt.setString(3, cliente.getApellidos());
                stmt.setString(4, cliente.getDni());
                stmt.setString(5,cliente.getNumeroCelular());
                stmt.setDate(6, Date.valueOf(cliente.getFechaNacimiento()));

                int filaAfectada = stmt.executeUpdate();

                if(filaAfectada == 0) {
                    return Optional.empty();
                }
                try(var rs = stmt.getGeneratedKeys()) {

                    if(rs.next()) {
                        cliente.setId(rs.getLong("id"));

                        return Optional.of(cliente);

                    }
                    return Optional.empty();

                }
            }

        });

    }

    @Override
    public boolean existsByDni(String dni) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(EXISTS_DNI)) {
                stmt.setString(1, dni);
                try (var rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public boolean existsByCelular(String celular) {

        return ejecutar(conn -> {
            try (var stmt = conn.prepareStatement(EXISTS_CELULAR)) {
                stmt.setString(1, celular);
                try (var rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    @Override
    public Optional<Cliente> findByCelular(String celular) {
        return ejecutar(conn -> buscarPorColumna(conn, FIND_BY_CELULAR, celular));
    }

    @Override
    public Optional<Cliente> findByUsuarioId(Long usuarioId) {
        return ejecutar(conn -> buscarPorColumna(conn, FIND_BY_USUARIO_ID, usuarioId));
    }

    private Optional<Cliente> buscarPorColumna(Connection conn, String sql, Object valor) throws SQLException {
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, valor);

            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return Cliente.reconstruir(
                rs.getLong("id"),
                rs.getLong("usuario_id"),
                rs.getString("nombres"),
                rs.getString("apellidos"),
                rs.getString("dni"),
                rs.getString("numero_celular"),
                rs.getDate("fecha_nacimiento").toLocalDate(),
                rs.getObject("fecha_registro", OffsetDateTime.class)
        );
    }
}
