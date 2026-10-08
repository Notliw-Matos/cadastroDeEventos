package application;

import application.ports.in.CriarAtividade;
import domain.exception.EstadoInvalidoException;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Atividade;
import domain.model.Evento;
import domain.model.Usuario;
import domain.ports.AtividadeRepository;
import domain.ports.EventoRepository;
import domain.ports.UsuarioRepository;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;

import java.util.UUID;

public class CriarAtividadeUseCase implements CriarAtividade {

    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public CriarAtividadeUseCase(AtividadeRepository atividadeRepository,
                                 EventoRepository eventoRepository,
                                 UsuarioRepository usuarioRepository) {
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Atividade executar(UUID solicitanteId, UUID eventoId, String titulo,
                              Atividade.TipoAtividade tipo, IntervaloTempo intervalo, Vagas vagas) {
        Usuario solicitante = usuarioRepository.buscarPorId(solicitanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        Evento evento = eventoRepository.buscarPorId(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));

        evento.exigirGerenciadoPor(solicitante);             // RN-18
        if (!evento.aceitaNovasAtividades()) {
            throw new EstadoInvalidoException("Não é possível adicionar atividades a um evento encerrado.");
        }

        Atividade atividade = Atividade.nova(eventoId, titulo, tipo, intervalo, vagas);
        atividadeRepository.salvar(atividade);
        return atividade;
    }
}
