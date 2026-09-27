package com.projects.application.port.out;


import com.projects.domain.model.billetera.Cliente;

import java.util.Optional;

public interface ClienteRepository {

    Optional<Cliente> save(Cliente cliente);
    boolean existsByDni(String dni);
    boolean existsByCelular(String celular);

    Optional<Cliente> findByCelular(String celular);

    /**
     * Puente entre la sesión (que solo conoce el usuarioId) y el cliente. Provisional:
     * cuando la sesión guarde directamente el clienteId, este método deja de ser necesario
     * para resolver la identidad en cada pantalla.
     */
    Optional<Cliente> findByUsuarioId(Long usuarioId);



}
