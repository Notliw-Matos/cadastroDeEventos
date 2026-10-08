package domain.model.avaliacao;

import domain.exception.ValorInvalidoException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Conjunto de respostas de UM participante a UM questionário (RF-26, RF-27, RN-13, RN-14).
 * A invariante central é garantida aqui: toda pergunta do questionário tem exatamente uma resposta
 * e cada resposta passou pela validação do tipo da pergunta — sem precisar de controller/serviço
 * para fazer essa checagem depois (ROO-02).
 */
public final class RespostaQuestionario {
    private final UUID id;
    private final UUID questionarioId;
    private final UUID participanteId;
    private final List<Resposta> respostas;

    /**
     * Fábrica que valida cada resposta contra sua pergunta (tipo + opções) antes de criar o objeto.
     * Se qualquer validação falhar, nenhum objeto é criado (atomicidade no domínio).
     */
    public static RespostaQuestionario criar(UUID id, Questionario questionario, UUID participanteId,
                                             Map<UUID, String> valoresPorPerguntaId) {
        if (participanteId == null) throw new ValorInvalidoException("Participante é obrigatório.");
        if (valoresPorPerguntaId == null || valoresPorPerguntaId.isEmpty())
            throw new ValorInvalidoException("É necessário responder ao menos uma pergunta.");

        List<Resposta> respostas = questionario.getPerguntas().stream().map(pergunta -> {
            String valor = valoresPorPerguntaId.get(pergunta.getId());
            if (valor == null)
                throw new ValorInvalidoException("Pergunta \"" + pergunta.getEnunciado() + "\" não foi respondida.");
            pergunta.validarResposta(valor); // delega ao tipo — ROO-05
            return new Resposta(null, pergunta.getId(), valor);
        }).collect(Collectors.toList());

        return new RespostaQuestionario(id, questionario.getId(), participanteId, respostas);
    }

    private RespostaQuestionario(UUID id, UUID questionarioId, UUID participanteId, List<Resposta> respostas) {
        this.id = id != null ? id : UUID.randomUUID();
        this.questionarioId = questionarioId;
        this.participanteId = participanteId;
        this.respostas = Collections.unmodifiableList(respostas);
    }

    public UUID getId() { return id; }
    public UUID getQuestionarioId() { return questionarioId; }
    public UUID getParticipanteId() { return participanteId; }
    public List<Resposta> getRespostas() { return respostas; }
}
