package application;

import domain.exception.RecursoNaoEncontradoException;
import domain.model.Atividade;
import domain.model.Evento;
import domain.model.Usuario;
import domain.model.avaliacao.Pergunta;
import domain.model.avaliacao.Questionario;
import domain.model.avaliacao.TipoPergunta;
import domain.ports.AtividadeRepository;
import domain.ports.EventoRepository;
import domain.ports.QuestionarioRepository;
import domain.ports.UsuarioRepository;

import java.util.List;
import java.util.UUID;

/** RF-24: organizador define título, perguntas e tipos de resposta. */
public class CriarQuestionarioUseCase {

    private final QuestionarioRepository questionarioRepository;
    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public CriarQuestionarioUseCase(QuestionarioRepository questionarioRepository, AtividadeRepository atividadeRepository,
                                     EventoRepository eventoRepository, UsuarioRepository usuarioRepository) {
        this.questionarioRepository = questionarioRepository;
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Questionario executar(UUID solicitanteId, UUID atividadeId, String titulo, List<Pergunta> perguntas) {
        Atividade atividade = atividadeRepository.buscarPorId(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada."));
        Evento evento = eventoRepository.buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        Usuario solicitante = usuarioRepository.buscarPorId(solicitanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        evento.exigirGerenciadoPor(solicitante); // RN-18

        Questionario questionario = new Questionario(null, atividadeId, titulo, perguntas);
        questionarioRepository.salvar(questionario);
        return questionario;
    }
}
