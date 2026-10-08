package application;

import application.ports.in.ListarEventosPublicos;
import domain.ports.EventoRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ListarEventosPublicosUseCase implements ListarEventosPublicos {

    private final EventoRepository eventoRepository;

    public ListarEventosPublicosUseCase(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public List<EventoPublicoDTO> executar() {
        return eventoRepository.listarPublicados().stream()
                .map(EventoPublicoDTO::de)
                .collect(Collectors.toList());
    }
}
