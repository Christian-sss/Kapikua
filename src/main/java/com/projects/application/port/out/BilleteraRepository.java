package com.projects.application.port.out;

import com.projects.domain.model.billetera.Billetera;

import java.util.Optional;

public interface BilleteraRepository {

    Optional<Billetera> save(Billetera billetera);



}
