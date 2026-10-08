package application;

import domain.exception.EstadoInvalidoException;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Inscricao;
import domain.model.avaliacao.Questionario;
import domain.model.avaliacao.RespostaQuestionario;
import domain.ports.InscricaoRepository;
import domain.ports.QuestionarioRepository;
import domain.ports.RegistroFrequenciaRepository;
import domain.ports.RespostaQuestionarioRepository;

import java.util.Map;
import java.util.UUID;

/** RF-26, RF-27, RN-13, RN-14: elegibilidade e unicidade de resposta. */
public class ResponderQuestionarioUseCase {

    private final RespostaQuestionarioRepository respostaRepository;
    private final QuestionarioRepository questionarioRepository;
    private final InscricaoRepository inscricaoRepository;
    private final RegistroFrequenciaRepository registroFrequenciaRepository;
    private final RegistrarPresencaUseCase registrarPresencaUseCase;

    public ResponderQuestionarioUseCase(RespostaQuestionarioRepository respostaRepository,
                                         QuestionarioRepository questionarioRepository,
                                         InscricaoRepository inscricaoRepository,
                                         RegistroFrequenciaRepository registroFrequenciaRepository,
                                         RegistrarPresencaUseCase registrarPresencaUseCase) {
        this.respostaRepository = respostaRepository;
        this.questionarioRepository = questionarioRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.registroFrequenciaRepository = registroFrequenciaRepository;
        this.registrarPresencaUseCase = registrarPresencaUseCase;
    }

    public RespostaQuestionario executar(UUID participanteId, UUID questionarioId, Map<UUID, String> valoresPorPergunta) {
        Questionario questionario = questionarioRepository.buscarPorId(questionarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Questionário não encontrado."));

        // RN-13: participante precisa estar inscrito E com presença validada
        Inscricao inscricao = inscricaoRepository.buscarPorParticipanteEAtividade(participanteId, questionario.getAtividadeId())
                .orElseThrow(() -> new EstadoInvalidoException(
                        "Você não está inscrito nesta atividade e não pode avaliar."));
        if (!inscricao.isAtiva())
            throw new EstadoInvalidoException("Sua inscrição está cancelada; não é possível avaliar.");

        boolean presente = registrarPresencaUseCase.calcularSituacaoDePresenca(inscricao.getId());
        if (!presente)
            throw new EstadoInvalidoException(
                    "Sua presença não foi validada nesta atividade. Só participantes presentes podem avaliar.");

        // RN-14: no máximo uma resposta por questionário por participante
        respostaRepository.buscarPorParticipanteEQuestionario(participanteId, questionarioId)
                .ifPresent(existente -> { throw new domain.exception.EstadoInvalidoException(
                        "Você já avaliou este questionário. Cada participante pode responder uma vez."); });

        // A validação de cada resposta (tipo + opções) acontece dentro do domínio (ROO-02)
        RespostaQuestionario resposta = RespostaQuestionario.criar(null, questionario, participanteId, valoresPorPergunta);
        respostaRepository.salvar(resposta);
        return resposta;
    }
}
