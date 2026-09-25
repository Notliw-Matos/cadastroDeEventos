package infrastructure.persistence;

import domain.model.Evento;
import domain.ports.EventoRepository;
import domain.vo.IntervaloTempo;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class EventoRepositoryDatabase implements EventoRepository {

    @Override
    public void salvar(Evento evento) {
        String sql = "INSERT INTO eventos (id, titulo, descricao, data_inicio, data_fim, local_ou_link) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET titulo = EXCLUDED.titulo, descricao = EXCLUDED.descricao, " +
                     "data_inicio = EXCLUDED.data_inicio, data_fim = EXCLUDED.data_fim, local_ou_link = EXCLUDED.local_ou_link;";

        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, evento.getId());
            stmt.setString(2, evento.getTitulo());
            stmt.setString(3, evento.getDescricao());
            stmt.setTimestamp(4, Timestamp.valueOf(evento.getPeriodo().getInicio()));
            stmt.setTimestamp(5, Timestamp.valueOf(evento.getPeriodo().getFim()));
            stmt.setString(6, evento.getLocal());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar evento no banco: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Evento> buscarPorId(UUID id) {
        String sql = "SELECT * FROM eventos WHERE id = ?";
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                IntervaloTempo periodo = new IntervaloTempo(
                        rs.getTimestamp("data_inicio").toLocalDateTime(),
                        rs.getTimestamp("data_fim").toLocalDateTime()
                );
                Evento evento = new Evento(
                        (UUID) rs.getObject("id"),
                        rs.getString("titulo"),
                        rs.getString("descricao"),
                        periodo,
                        rs.getString("local_ou_link")
                );
                return Optional.of(evento);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar evento: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
}