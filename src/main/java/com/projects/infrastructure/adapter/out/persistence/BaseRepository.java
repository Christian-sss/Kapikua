package com.projects.infrastructure.adapter.out.persistence;

import com.projects.infrastructure.config.PostressqlConexion;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseRepository {

    protected <T> T ejecutar(SqlWork<T> trabajo) {
        Connection prestada = ConexionContexto.get();
        boolean esPropia = prestada == null;
        Connection conn = prestada;

        try  {
            if(esPropia) {
                conn = PostressqlConexion.getConnection();
            }

            return trabajo.hacer(conn);

        } catch (SQLException ex) {
            throw new RuntimeException("Error de acceso a datos", ex);
        } finally {
            if(esPropia && conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }

    }


    @FunctionalInterface
    protected interface SqlWork<T> {
        T hacer(Connection conn) throws SQLException;
    }


}
