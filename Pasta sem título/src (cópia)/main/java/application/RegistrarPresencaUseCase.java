package application;

import domain.exception.EstadoInvalidoException;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Atividade;
import domain.model.Evento;
import domain.model.Inscricao;
import domain.model.RegistroPresenca;
import domain.model.Usuario;
import domain.ports.AtividadeRepository;
import domain.ports.EventoRepository;
import domain.ports.InscricaoRepository;
import domain.ports.RegistroFrequenciaRepository;
import domain.ports.UsuarioRepository;

import java.util.List;
import java.util.UUID;

/**
 * RF-19 a RF-23. Antes (S2) este caso de uso guardava os registros numa ArrayList interna (não
 * sobrevivia a reinício nem passava pela API) e exigia que QUEM CHAMASSE soubesse qual
 * {@code ValidadorFrequenciaStrategy} usar. Agora ele persiste pela porta
 * {@link RegistroFrequenciaRepository}, resolve o critério sozinho (ROO-05: quem decide o
 * polimorfismo é o domínio) e verifica autorização do mesmo jeito que {@code CriarAtividadeUseCase}
 * (RN-18: só quem gerencia o evento, ou administrador, registra presença nele).
 */
public class RegistrarPresencaUseCase {

    private final InscricaoRepository inscricaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroFrequenciaRepository registroFrequenciaRepository;

    public RegistrarPresencaUseCase(InscricaoRepository inscricaoRepository, AtividadeRepository atividadeRepository,
                                    EventoRepository eventoRepository, UsuarioRepository usuarioRepository,
                                    RegistroFrequenciaRepository registroFrequenciaRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.registroFrequenciaRepository = registroFrequenciaRepository;
    }

    /** RF-20/21 (via QR) e RF-22 (lançamento manual) chegam aqui — só muda o {@code tipo} e quem opera. */
    public RegistroPresenca registrarMarcacao(UUID inscricaoId, RegistroPresenca.TipoMarcacao tipo, UUID operadoPorId) {
        Inscricao inscricao = buscarInscricaoAtiva(inscricaoId);
        Atividade atividade = atividadeRepository.buscarPorId(inscricao.getAtividadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada."));
        Evento evento = eventoRepository.buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        Usuario operador = usuarioRepository.buscarPorId(operadoPorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        evento.exigirGerenciadoPor(operador); // RN-18: só o organizador do evento (ou admin)

        RegistroPresenca novoRegistro = new RegistroPresenca(null, inscricaoId, tipo, operadoPorId);
        registroFrequenciaRepository.salvar(novoRegistro);
        return novoRegistro;
    }

    /** RF-23: situação calculada a partir dos registros persistidos e do critério da atividade. */
    public boolean calcularSituacaoDePresenca(UUID inscricaoId) {
        Inscricao inscricao = inscricaoRepository.buscarPorId(inscricaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Inscrição não encontrada."));
        Atividade atividade = atividadeRepository.buscarPorId(inscricao.getAtividadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada."));

        List<RegistroPresenca> registros = registroFrequenciaRepository.listarPorInscricao(inscricaoId);
        return atividade.getCriterioFrequencia().validador().isPresencaValida(registros);
    }

    private Inscricao buscarInscricaoAtiva(UUID inscricaoId) {
        Inscricao inscricao = inscricaoRepository.buscarPorId(inscricaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Inscrição não encontrada."));
        if (!inscricao.isAtiva()) {
            throw new EstadoInvalidoException("Não é possível registrar presença para uma inscrição cancelada.");
        }
        return inscricao;
    }
}
