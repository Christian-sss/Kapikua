package com.projects.infrastructure.adapter.out.persistence;



import com.projects.application.port.out.ClienteRepository;
import com.projects.application.port.out.TransactionRepository;
import com.projects.application.port.out.UsuarioRepository;
import com.projects.domain.model.billetera.Cliente;
import com.projects.domain.model.seguridad.Usuario;
import com.projects.infrastructure.config.PostressqlConexion;

import java.sql.SQLException;
import java.util.Optional;

public class PgPersistenceAdapter implements TransactionRepository, UsuarioRepository, ClienteRepository {

    private static final String SAVE_USER = """
            """;


    @Override
    public Optional<Cliente> save(Cliente cliente) {
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> save(Usuario usuario) {
        try(var con= PostressqlConexion.getConnection();
            var stmt = con.prepareStatement(SAVE_USER)
        ) {


        } catch (SQLException ex) {
            ex.printStackTrace();
        }


        return Optional.empty();
    }
}
