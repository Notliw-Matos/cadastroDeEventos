package infrastructure.memoria;

import domain.model.avaliacao.RespostaQuestionario;
import domain.ports.RespostaQuestionarioRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RespostaQuestionarioRepositoryEmMemoria implements RespostaQuestionarioRepository {
    private final Map<UUID, RespostaQuestionario> porId = new LinkedHashMap<>();

    @Override public void salvar(RespostaQuestionario r) { porId.put(r.getId(), r); }

    @Override public Optional<RespostaQuestionario> buscarPorParticipanteEQuestionario(UUID participanteId, UUID questionarioId) {
        return porId.values().stream()
                .filter(r -> r.getParticipanteId().equals(participanteId) && r.getQuestionarioId().equals(questionarioId))
                .findFirst();
    }

    @Override public List<RespostaQuestionario> listarPorQuestionario(UUID questionarioId) {
        List<RespostaQuestionario> resultado = new ArrayList<>();
        for (RespostaQuestionario r : porId.values())
            if (r.getQuestionarioId().equals(questionarioId)) resultado.add(r);
        return resultado;
    }
}
