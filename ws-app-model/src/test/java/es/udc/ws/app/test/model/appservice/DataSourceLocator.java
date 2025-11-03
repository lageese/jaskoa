package es.udc.ws.app.test.model.appservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataSourceLocator {

    // Método init que recibe filename (simulado)
    public static void init(String filename) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Método que devuelve una conexión
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost/wstest?useSSL=false&serverTimezone=Europe/Madrid&allowPublicKeyRetrieval=true",
                "ws",
                "ws");
    }
}