package infrastructure.persistence;

import domain.model.RegistroPresenca;
import domain.ports.RegistroFrequenciaRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Adaptador de saída (Postgres). Schema confirmado no Supabase real — sem migração necessária. */
public class RegistroFrequenciaRepositoryDatabase implements RegistroFrequenciaRepository {

    @Override
    public void salvar(RegistroPresenca registro) {
        String sql = "INSERT INTO registros_frequencia (id, inscricao_id, data_hora, tipo, operado_por) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, registro.getId());
            stmt.setObject(2, registro.getInscricaoId());
            stmt.setObject(3, registro.getDataHora());
            stmt.setString(4, registro.getTipo().name());
            stmt.setObject(5, registro.getOperadorId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar registro de frequência: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RegistroPresenca> listarPorInscricao(UUID inscricaoId) {
        List<RegistroPresenca> resultado = new ArrayList<>();
        String sql = "SELECT id, inscricao_id, data_hora, tipo, operado_por FROM registros_frequencia WHERE inscricao_id = ?";
        try (Connection conn = ConexaoDatabase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, inscricaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new RegistroPresenca(
                            rs.getObject("id", UUID.class),
                            rs.getObject("inscricao_id", UUID.class),
                            RegistroPresenca.TipoMarcacao.valueOf(rs.getString("tipo")),
                            rs.getObject("data_hora", java.time.LocalDateTime.class),
                            rs.getObject("operado_por", UUID.class)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar registros de frequência: " + e.getMessage(), e);
        }
        return resultado;
    }
}
