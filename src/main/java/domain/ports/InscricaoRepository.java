package domain.ports;

import domain.model.Inscricao;
import java.util.Optional;
import java.util.UUID;

public interface InscricaoRepository {
    void salvar(Inscricao inscricao);
    Optional<Inscricao> buscarPorId(UUID id);
    Optional<Inscricao> buscarPorParticipanteEAtividade(UUID participanteId, UUID atividadeId);
}