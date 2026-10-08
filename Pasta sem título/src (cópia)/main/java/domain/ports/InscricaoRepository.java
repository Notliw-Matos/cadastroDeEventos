package domain.ports;

import domain.model.Inscricao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InscricaoRepository {
    void salvar(Inscricao inscricao);
    Optional<Inscricao> buscarPorId(UUID id);
    Optional<Inscricao> buscarPorParticipanteEAtividade(UUID participanteId, UUID atividadeId);
    List<Inscricao> listarPorParticipante(UUID participanteId);

    /** RF-29/30: todos os inscritos (ativos e cancelados) de uma atividade, para relatório. */
    List<Inscricao> listarPorAtividade(UUID atividadeId);
}
