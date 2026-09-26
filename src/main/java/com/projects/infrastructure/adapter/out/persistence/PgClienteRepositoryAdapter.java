package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.ClienteRepository;
import com.projects.domain.model.billetera.Cliente;


import java.sql.Statement;
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

    private static final String SAVE_CLIENTE = """
            
            INSERT INTO billetera.cliente (usuario_id, nombres, apellidos, dni, numero_celular)
            VALUES (?,?,?,?,?);
   
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
}
