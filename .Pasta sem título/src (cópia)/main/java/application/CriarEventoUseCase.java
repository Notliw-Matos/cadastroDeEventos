package application;

import application.ports.in.CriarEvento;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Evento;
import domain.model.Usuario;
import domain.ports.EventoRepository;
import domain.ports.UsuarioRepository;
import domain.vo.IntervaloTempo;

import java.util.UUID;

public class CriarEventoUseCase implements CriarEvento {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    public CriarEventoUseCase(EventoRepository eventoRepository, UsuarioRepository usuarioRepository) {
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Evento executar(UUID solicitanteId, String titulo, String descricao, IntervaloTempo periodo, String local) {
        Usuario solicitante = usuarioRepository.buscarPorId(solicitanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        solicitante.exigirPermissaoParaGerenciarEventos();   // RNF-06: checagem no servidor

        Evento evento = Evento.novo(solicitante.getId(), titulo, descricao, periodo, local);
        eventoRepository.salvar(evento);
        return evento;
    }
}
