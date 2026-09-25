package infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDatabase {

    // URL no formato JDBC do PostgreSQL apontando para o teu projeto no Supabase
    
    private static final String URL = "jdbc:postgresql://db.jrqlzhxbpfodteflnypu.supabase.co:5432/postgres";
    private static final String USER = "postgres";
    
    // Substitui pela password do teu projeto criada no Supabase
    private static final String PASSWORD = "PROJETOPOO2026";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}