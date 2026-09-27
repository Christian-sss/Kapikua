package com.projects.application.port.out;

import java.util.Optional;

public interface TipoTransaccionRepository {

    Optional<Long> findIdByCodigo(String codigo);

}
