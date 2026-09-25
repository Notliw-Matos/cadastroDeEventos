package infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDatabase {

    // Usando o Session Pooler do Supabase (compatível com redes IPv4,
    // ao contrário da conexão direta que exige IPv6)
    private static final String URL = "jdbc:postgresql://aws-1-us-east-2.pooler.supabase.com:5432/postgres";
    private static final String USER = "postgres.jrqlzhxbpfodteflnypu";

    // TODO: mover para variável de ambiente antes de comitar (não deixar senha em texto puro no código)
    private static final String PASSWORD = "PROJETOPOO2026";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}