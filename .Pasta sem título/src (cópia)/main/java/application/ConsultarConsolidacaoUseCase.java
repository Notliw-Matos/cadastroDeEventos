package application;

import domain.exception.AcessoNegadoException;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Atividade;
import domain.model.Evento;
import domain.model.Usuario;
import domain.model.avaliacao.Questionario;
import domain.model.avaliacao.RespostaQuestionario;
import domain.ports.AtividadeRepository;
import domain.ports.EventoRepository;
import domain.ports.QuestionarioRepository;
import domain.ports.RespostaQuestionarioRepository;
import domain.ports.UsuarioRepository;

import java.util.List;
import java.util.UUID;

/** RF-28: organizador consulta resultados consolidados. */
public class ConsultarConsolidacaoUseCase {

    private final QuestionarioRepository questionarioRepository;
    private final RespostaQuestionarioRepository respostaRepository;
    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public ConsultarConsolidacaoUseCase(QuestionarioRepository questionarioRepository,
                                         RespostaQuestionarioRepository respostaRepository,
                                         AtividadeRepository atividadeRepository,
                                         EventoRepository eventoRepository, UsuarioRepository usuarioRepository) {
        this.questionarioRepository = questionarioRepository;
        this.respostaRepository = respostaRepository;
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ConsolidacaoAvaliacaoDTO executar(UUID solicitanteId, UUID questionarioId) {
        Questionario questionario = questionarioRepository.buscarPorId(questionarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Questionário não encontrado."));
        Atividade atividade = atividadeRepository.buscarPorId(questionario.getAtividadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada."));
        Evento evento = eventoRepository.buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        Usuario solicitante = usuarioRepository.buscarPorId(solicitanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        if (!evento.podeSerGerenciadoPor(solicitante))
            throw new AcessoNegadoException("Só o organizador do evento pode consultar os resultados.");

        List<RespostaQuestionario> respostas = respostaRepository.listarPorQuestionario(questionarioId);
        return ConsolidacaoAvaliacaoDTO.calcular(questionario, respostas);
    }
}
