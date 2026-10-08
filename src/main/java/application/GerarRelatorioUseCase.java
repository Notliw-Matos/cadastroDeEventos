package application;

import application.relatorio.RelatorioExportavel;
import application.relatorio.RelatorioFrequencia;
import application.relatorio.RelatorioInscricoes;
import domain.exception.AcessoNegadoException;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Atividade;
import domain.model.Evento;
import domain.model.Usuario;
import domain.ports.AtividadeRepository;
import domain.ports.EventoRepository;
import domain.ports.InscricaoRepository;
import domain.ports.RegistroFrequenciaRepository;
import domain.ports.UsuarioRepository;

import java.util.UUID;

/** RF-29, RF-30, RF-31. */
public class GerarRelatorioUseCase {

    public enum TipoRelatorio { INSCRICOES, FREQUENCIA }

    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;
    private final RegistroFrequenciaRepository registroFrequenciaRepository;

    public GerarRelatorioUseCase(InscricaoRepository inscricaoRepository, UsuarioRepository usuarioRepository,
                                  AtividadeRepository atividadeRepository, EventoRepository eventoRepository,
                                  RegistroFrequenciaRepository registroFrequenciaRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
        this.registroFrequenciaRepository = registroFrequenciaRepository;
    }

    public RelatorioExportavel executar(UUID solicitanteId, UUID atividadeId, TipoRelatorio tipo) {
        Atividade atividade = atividadeRepository.buscarPorId(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada."));
        Evento evento = eventoRepository.buscarPorId(atividade.getEventoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        Usuario solicitante = usuarioRepository.buscarPorId(solicitanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        if (!evento.podeSerGerenciadoPor(solicitante))
            throw new AcessoNegadoException("Só o organizador do evento pode gerar relatórios.");

        var inscricoes = inscricaoRepository.listarPorAtividade(atividadeId);

        if (tipo == TipoRelatorio.INSCRICOES)
            return new RelatorioInscricoes(inscricoes, usuarioRepository, atividade.getTitulo());
        else
            return new RelatorioFrequencia(inscricoes, usuarioRepository, registroFrequenciaRepository, atividade);
    }
}
