package domain.ports;

import domain.model.avaliacao.Questionario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionarioRepository {
    void salvar(Questionario questionario);
    Optional<Questionario> buscarPorId(UUID id);
    List<Questionario> listarPorAtividade(UUID atividadeId);
}
