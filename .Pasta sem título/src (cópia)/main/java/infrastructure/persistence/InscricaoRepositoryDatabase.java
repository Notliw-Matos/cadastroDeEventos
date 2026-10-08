package infrastructure.persistence;

import domain.model.Inscricao;
import domain.ports.InscricaoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InscricaoRepositoryDatabase implements InscricaoRepository {

    private static final String SELECT_BASE =
            "SELECT id, participante_id, atividade_id, status FROM inscricoes ";

    @Override
    public void salvar(Inscricao inscricao) {
        String sql = "INSERT INTO inscricoes (id, participante_id, atividade_id, status) VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status";
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricao.getId());
            stmt.setObject(2, inscricao.getParticipanteId());
            stmt.setObject(3, inscricao.getAtividadeId());
            stmt.setString(4, inscricao.getSituacao().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar inscrição no banco: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Inscricao> buscarPorId(UUID id) {
        return buscarUm(SELECT_BASE + "WHERE id = ?", id);
    }

    @Override
    public Optional<Inscricao> buscarPorParticipanteEAtividade(UUID participanteId, UUID atividadeId) {
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_BASE + "WHERE participante_id = ? AND atividade_id = ?")) {
            stmt.setObject(1, participanteId);
            stmt.setObject(2, atividadeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar inscrição: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Inscricao> listarPorParticipante(UUID participanteId) {
        List<Inscricao> resultado = new ArrayList<>();
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE participante_id = ?")) {
            stmt.setObject(1, participanteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar inscrições do participante: " + e.getMessage(), e);
        }
        return resultado;
    }

    private Optional<Inscricao> buscarUm(String sql, UUID id) {
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar inscrição: " + e.getMessage(), e);
        }
    }

    private Inscricao mapear(ResultSet rs) throws SQLException {
        return new Inscricao(
                rs.getObject("id", UUID.class),
                rs.getObject("participante_id", UUID.class),
                rs.getObject("atividade_id", UUID.class),
                Inscricao.Situacao.valueOf(rs.getString("status")));
    }
    @Override
    public List<Inscricao> listarPorAtividade(UUID atividadeId) {
        List<Inscricao> resultado = new ArrayList<>();
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE atividade_id = ?")) {
            stmt.setObject(1, atividadeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar inscrições da atividade: " + e.getMessage(), e);
        }
        return resultado;
    }
}
