package infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDatabase {

<<<<<<< HEAD
    // Usando o Session Pooler do Supabase (compatível com redes IPv4,
    // ao contrário da conexão direta que exige IPv6)
    private static final String URL = "jdbc:postgresql://aws-1-us-east-2.pooler.supabase.com:5432/postgres";
    private static final String USER = "postgres.jrqlzhxbpfodteflnypu";

    // TODO: mover para variável de ambiente antes de comitar (não deixar senha em texto puro no código)
=======
    // URL no formato JDBC do PostgreSQL apontando para o teu projeto no Supabase
    private static final String URL = "jdbc:postgresql://db.jrqlzhxbpfodteflnypu.supabase.co:5432/postgres";
    private static final String USER = "postgres";
    
    // Substitui pela password do teu projeto criada no Supabase
>>>>>>> 34be944cac9cbfed40e80f19ebf533eb28e7f782
    private static final String PASSWORD = "PROJETOPOO2026";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}