package com.projects.infrastructure.adapter.out.persistence;

import com.projects.application.port.out.TransactionManager;
import com.projects.domain.result.Result;
import com.projects.infrastructure.config.PostressqlConexion;

import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionManagerAdapter implements TransactionManager {


    @Override
    public <T> Result<T> enTransaccion(Supplier<Result<T>> work) {

        try (var conn = PostressqlConexion.getConnection()) {


            conn.setAutoCommit(false);
            ConexionContexto.cambiar(conn);

            try {
                Result<T> result = work.get();

                if(result.isFailure()) {
                    conn.rollback();
                } else {
                    conn.commit();
                }

                return result;
            } catch (RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                ConexionContexto.clear();
            }

        } catch (SQLException ex) {

            throw new RuntimeException("ERROR EN LA TRANSACCION", ex);

        }

    }
}
