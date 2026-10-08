package application;

import application.ports.in.ListarAtividadesDoEvento;
import domain.exception.RecursoNaoEncontradoException;
import domain.ports.AtividadeRepository;
import domain.ports.EventoRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListarAtividadesDoEventoUseCase implements ListarAtividadesDoEvento {

    private final AtividadeRepository atividadeRepository;
    private final EventoRepository eventoRepository;

    public ListarAtividadesDoEventoUseCase(AtividadeRepository atividadeRepository, EventoRepository eventoRepository) {
        this.atividadeRepository = atividadeRepository;
        this.eventoRepository = eventoRepository;
    }

    @Override
    public List<AtividadeDTO> executar(UUID eventoId) {
        eventoRepository.buscarPorId(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado."));
        return atividadeRepository.listarPorEvento(eventoId).stream()
                .map(AtividadeDTO::de)
                .collect(Collectors.toList());
    }
}
