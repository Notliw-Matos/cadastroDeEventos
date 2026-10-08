package infrastructure.persistence;

import domain.model.Evento;
import domain.ports.EventoRepository;
import domain.vo.IntervaloTempo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de saída (Postgres). Schema confirmado em 30/09 no Supabase real do projeto +
 * migração de organizador_id/estado (ver docs/S4_ENTREGA.md, seção "Migração de banco").
 *
 * Simplificação registrada: as colunas são "timestamp without time zone" (não guardam fuso).
 * A plataforma assume um único fuso para todos os eventos (Evento.FUSO_PADRAO = America/Sao_Paulo)
 * em vez de guardar o fuso por evento — suficiente para o núcleo obrigatório e mais simples que
 * converter fuso a fuso no SQL.
 */
public class EventoRepositoryDatabase implements EventoRepository {

    private static final String SELECT_BASE =
            "SELECT id, titulo, descricao, data_inicio, data_fim, local_ou_link, organizador_id, estado FROM eventos ";

    @Override
    public void salvar(Evento evento) {
        String sql = "INSERT INTO eventos (id, titulo, descricao, data_inicio, data_fim, local_ou_link, organizador_id, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET titulo = EXCLUDED.titulo, descricao = EXCLUDED.descricao, " +
                     "data_inicio = EXCLUDED.data_inicio, data_fim = EXCLUDED.data_fim, " +
                     "local_ou_link = EXCLUDED.local_ou_link, estado = EXCLUDED.estado";

        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, evento.getId());
            stmt.setString(2, evento.getTitulo());
            stmt.setString(3, evento.getDescricao());
            stmt.setObject(4, evento.getPeriodo().getInicio());
            stmt.setObject(5, evento.getPeriodo().getFim());
            stmt.setString(6, evento.getLocalOuModalidade());
            stmt.setObject(7, evento.getOrganizadorId());
            stmt.setString(8, evento.getEstado().name());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar evento no banco: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Evento> buscarPorId(UUID id) {
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar evento: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Evento> listarPublicados() {
        List<Evento> resultado = new ArrayList<>();
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_BASE + "WHERE estado = 'PUBLICADO' ORDER BY data_inicio")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar eventos publicados: " + e.getMessage(), e);
        }
        return resultado;
    }

    private Evento mapear(ResultSet rs) throws SQLException {
        IntervaloTempo periodo = new IntervaloTempo(
                rs.getObject("data_inicio", LocalDateTime.class),
                rs.getObject("data_fim", LocalDateTime.class));
        return new Evento(
                rs.getObject("id", UUID.class),
                rs.getString("titulo"),
                rs.getString("descricao"),
                periodo,
                rs.getString("local_ou_link"),
                Evento.Estado.valueOf(rs.getString("estado")),
                Evento.FUSO_PADRAO,
                rs.getObject("organizador_id", UUID.class));
    }
}
