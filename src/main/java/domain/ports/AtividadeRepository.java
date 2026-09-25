package domain.ports;

import domain.model.Atividade;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface AtividadeRepository {
    void salvar(Atividade atividade);
    Optional<Atividade> buscarPorId(UUID id);
    List<Atividade> listarPorEvento(UUID eventoId);
}