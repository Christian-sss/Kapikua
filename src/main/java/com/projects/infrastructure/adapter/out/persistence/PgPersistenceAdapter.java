package com.projects.infrastructure.adapter.out.persistence;



import com.projects.application.port.out.ClienteRepository;
import com.projects.application.port.out.TransactionRepository;
import com.projects.application.port.out.UsuarioRepository;
import com.projects.domain.model.billetera.Cliente;
import com.projects.domain.model.seguridad.Usuario;

import java.util.Optional;

public class PgPersistenceAdapter implements TransactionRepository,ClienteRepository,UsuarioRepository{

    private static final String SAVE_USER = """
            """;

    @Override
    public Optional<Cliente> save(Cliente cliente) {
        return Optional.empty();
    }

    @Override
    public boolean existsByDni(String dni) {
        return false;
    }

    @Override
    public boolean existsByCelular(String celular) {
        return false;
    }

    @Override
    public Optional<Usuario> save(Usuario usuario) {
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }
}
