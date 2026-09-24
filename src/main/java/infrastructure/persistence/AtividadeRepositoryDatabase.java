package infrastructure.persistence;

import domain.model.Atividade;
import domain.ports.AtividadeRepository;
import domain.vo.IntervaloTempo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AtividadeRepositoryDatabase implements AtividadeRepository {

    @Override
    public void salvar(Atividade atividade) {
        String sql = "INSERT INTO atividades (id, evento_id, titulo, tipo, data_hora_inicio, data_hora_fim, local, capacidade_maxima, total_inscritos) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET total_inscritos = EXCLUDED.total_inscritos;";

        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, atividade.getId());
            stmt.setObject(2, atividade.getEventoId());
            stmt.setString(3, atividade.getTitulo());
            stmt.setString(4, atividade.getTipo().name());
            stmt.setTimestamp(5, Timestamp.valueOf(atividade.getIntervalo().getInicio()));
            stmt.setTimestamp(6, Timestamp.valueOf(atividade.getIntervalo().getFim()));
            stmt.setString(7, atividade.getLocal());
            stmt.setInt(8, atividade.getCapacidadeMaxima());
            stmt.setInt(9, atividade.getTotalInscritos());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar atividade no Supabase: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Atividade> buscarPorId(UUID id) {
        String sql = "SELECT * FROM atividades WHERE id = ?";
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                IntervaloTempo intervalo = new IntervaloTempo(
                        rs.getTimestamp("data_hora_inicio").toLocalDateTime(),
                        rs.getTimestamp("data_hora_fim").toLocalDateTime()
                );
                Atividade atividade = new Atividade(
                        (UUID) rs.getObject("id"),
                        (UUID) rs.getObject("evento_id"),
                        rs.getString("titulo"),
                        Atividade.TipoAtividade.valueOf(rs.getString("tipo")),
                        intervalo,
                        rs.getString("local"),
                        rs.getInt("capacidade_maxima")
                );
                return Optional.of(atividade);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar atividade: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Atividade> listarPorEvento(UUID eventoId) {
        List<Atividade> atividades = new ArrayList<>();
        String sql = "SELECT * FROM atividades WHERE evento_id = ?";
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, eventoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                IntervaloTempo intervalo = new IntervaloTempo(
                        rs.getTimestamp("data_hora_inicio").toLocalDateTime(),
                        rs.getTimestamp("data_hora_fim").toLocalDateTime()
                );
                Atividade atividade = new Atividade(
                        (UUID) rs.getObject("id"),
                        (UUID) rs.getObject("evento_id"),
                        rs.getString("titulo"),
                        Atividade.TipoAtividade.valueOf(rs.getString("tipo")),
                        intervalo,
                        rs.getString("local"),
                        rs.getInt("capacidade_maxima")
                );
                atividades.add(atividade);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar atividades: " + e.getMessage(), e);
        }
        return atividades;
    }
}