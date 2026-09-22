package com.projects.infrastructure.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostressqlConexion {

    private static final String URL = "";
    private static final String USER = "";
    private static final String PASS = "";




    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(URL,USER,PASS);


    }





}
