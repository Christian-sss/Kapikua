package com.projects.application.port.out;


import com.projects.domain.model.billetera.Cliente;

import java.util.Optional;

public interface ClienteRepository {

    Optional<Cliente> save(Cliente cliente);


}
