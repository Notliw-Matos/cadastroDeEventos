package infrastructure.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Configuração 100% por variáveis de ambiente — nenhuma senha nem host fica no código/Git.
 *
 *   DB_URL       (obrigatória) ex.: jdbc:postgresql://SEU-HOST.pooler.supabase.com:5432/postgres
 *   DB_USER      (obrigatória) ex.: postgres.jrqlzhxbpfodteflnypu
 *   DB_PASSWORD  (obrigatória)
 *
 * Onde encontrar: no painel do Supabase, Project Settings → Database → Connection string →
 * aba "Session pooler" (ou "Transaction pooler"). O host e o usuário de lá vão direto para
 * DB_URL/DB_USER; a senha do banco (a que a equipe definiu ao criar o projeto) vai para
 * DB_PASSWORD. Nunca cole a senha direto na URL nem no código.
 */
public class ConexaoDatabase {

    public static Connection getConnection() throws SQLException {
        String url = exigir("DB_URL");
        String usuario = exigir("DB_USER");
        String senha = exigir("DB_PASSWORD");
        return DriverManager.getConnection(url, usuario, senha);
    }

    /** Usado pelos testes de integração para decidir se há banco configurado. */
    public static boolean estaConfigurado() {
        return System.getenv("DB_URL") != null && System.getenv("DB_USER") != null
                && System.getenv("DB_PASSWORD") != null;
    }

    private static String exigir(String nome) {
        String valor = System.getenv(nome);
        if (valor == null || valor.isEmpty()) {
            throw new IllegalStateException("Defina a variável de ambiente " + nome
                    + " (veja o comentário desta classe ou docs/S4_ENTREGA.md).");
        }
        return valor;
    }
}
