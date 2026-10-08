package domain.ports;

import domain.model.avaliacao.RespostaQuestionario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RespostaQuestionarioRepository {
    void salvar(RespostaQuestionario resposta);
    Optional<RespostaQuestionario> buscarPorParticipanteEQuestionario(UUID participanteId, UUID questionarioId);
    List<RespostaQuestionario> listarPorQuestionario(UUID questionarioId);
}
