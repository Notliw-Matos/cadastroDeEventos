package infrastructure.persistence;

import domain.model.Atividade;
import domain.model.CriterioFrequencia;
import domain.ports.AtividadeRepository;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de saída (Postgres). Schema confirmado em 30/09 + migração de
 * criterio_frequencia/trilha_categoria (ver docs/S4_ENTREGA.md).
 *
 * Diferente da primeira tentativa (S2): "total_inscritos" é uma COLUNA de verdade no banco real
 * (não derivada por subquery). O domínio já mantém esse total no objeto Atividade; este
 * adaptador só grava/lê o valor que o objeto já calculou — uma fonte de verdade só, no domínio.
 */
public class AtividadeRepositoryDatabase implements AtividadeRepository {

    private static final String SELECT_BASE =
            "SELECT id, evento_id, titulo, tipo, trilha_categoria, local, data_hora_inicio, data_hora_fim, " +
            "capacidade_maxima, total_inscritos, criterio_frequencia FROM atividades ";

    @Override
    public void salvar(Atividade atividade) {
        String sql = "INSERT INTO atividades (id, evento_id, titulo, tipo, trilha_categoria, local, " +
                     "data_hora_inicio, data_hora_fim, capacidade_maxima, total_inscritos, criterio_frequencia) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET titulo = EXCLUDED.titulo, tipo = EXCLUDED.tipo, " +
                     "trilha_categoria = EXCLUDED.trilha_categoria, local = EXCLUDED.local, " +
                     "data_hora_inicio = EXCLUDED.data_hora_inicio, data_hora_fim = EXCLUDED.data_hora_fim, " +
                     "capacidade_maxima = EXCLUDED.capacidade_maxima, total_inscritos = EXCLUDED.total_inscritos, " +
                     "criterio_frequencia = EXCLUDED.criterio_frequencia";

        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, atividade.getId());
            stmt.setObject(2, atividade.getEventoId());
            stmt.setString(3, atividade.getTitulo());
            stmt.setString(4, atividade.getTipo().name());
            stmt.setString(5, atividade.getTrilhaCategoria());
            stmt.setString(6, atividade.getLocal());
            stmt.setObject(7, atividade.getIntervalo().getInicio());
            stmt.setObject(8, atividade.getIntervalo().getFim());
            if (atividade.getVagas().getLimite() == null) stmt.setNull(9, Types.INTEGER);
            else stmt.setInt(9, atividade.getVagas().getLimite());
            stmt.setInt(10, atividade.getTotalInscritos());
            stmt.setString(11, atividade.getCriterioFrequencia().name());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar atividade no banco: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Atividade> buscarPorId(UUID id) {
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE id = ?")) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar atividade: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Atividade> listarPorEvento(UUID eventoId) {
        List<Atividade> resultado = new ArrayList<>();
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_BASE + "WHERE evento_id = ? ORDER BY data_hora_inicio")) {
            stmt.setObject(1, eventoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar atividades: " + e.getMessage(), e);
        }
        return resultado;
    }

    private Atividade mapear(ResultSet rs) throws SQLException {
        IntervaloTempo intervalo = new IntervaloTempo(
                rs.getObject("data_hora_inicio", LocalDateTime.class),
                rs.getObject("data_hora_fim", LocalDateTime.class));
        int limite = rs.getInt("capacidade_maxima");
        Integer limiteOuNulo = rs.wasNull() ? null : limite;

        return new Atividade(
                rs.getObject("id", UUID.class),
                rs.getObject("evento_id", UUID.class),
                rs.getString("titulo"),
                Atividade.TipoAtividade.valueOf(rs.getString("tipo")),
                rs.getString("trilha_categoria"),
                rs.getString("local"),
                intervalo,
                Vagas.doLimite(limiteOuNulo),
                CriterioFrequencia.valueOf(rs.getString("criterio_frequencia")),
                rs.getInt("total_inscritos"));
    }
}
