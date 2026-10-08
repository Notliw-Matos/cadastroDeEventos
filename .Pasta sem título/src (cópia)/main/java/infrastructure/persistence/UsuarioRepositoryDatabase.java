package infrastructure.persistence;

import domain.exception.EmailJaCadastradoException;
import domain.model.Usuario;
import domain.ports.UsuarioRepository;
import domain.vo.Email;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class UsuarioRepositoryDatabase implements UsuarioRepository {

    private static final String UNIQUE_VIOLATION = "23505";

    @Override
    public void salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (id, nome, email, senha_hash, perfil) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET nome = EXCLUDED.nome, senha_hash = EXCLUDED.senha_hash";
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, usuario.getId());
            stmt.setString(2, usuario.getNome());
            stmt.setString(3, usuario.getEmail().getEndereco());
            stmt.setString(4, usuario.getSenhaHash());
            stmt.setString(5, usuario.getPerfil().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Duas requisições simultâneas podem passar pela checagem prévia; o banco é a última barreira (RN-01).
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new EmailJaCadastradoException();
            }
            throw new RuntimeException("Erro ao salvar usuário no banco: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return buscar("SELECT id, nome, email, senha_hash, perfil FROM usuarios WHERE id = ?", id);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
        return buscar("SELECT id, nome, email, senha_hash, perfil FROM usuarios WHERE email = ?",
                email.getEndereco());
    }

    @Override
    public boolean existePorEmail(Email email) {
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM usuarios WHERE email = ?")) {
            stmt.setString(1, email.getEndereco());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar e-mail: " + e.getMessage(), e);
        }
    }

    private Optional<Usuario> buscar(String sql, Object parametro) {
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Usuario(
                            rs.getObject("id", UUID.class),
                            rs.getString("nome"),
                            new Email(rs.getString("email")),
                            rs.getString("senha_hash"),
                            Usuario.Perfil.valueOf(rs.getString("perfil"))));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
}
