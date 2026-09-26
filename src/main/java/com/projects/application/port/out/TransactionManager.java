package com.projects.application.port.out;

import com.projects.domain.result.Result;

import java.util.function.Supplier;

public interface TransactionManager {

    <T>Result <T> enTransaccion(Supplier <Result<T>> work);


}
