package application;

import domain.model.Evento;
import domain.ports.EventoRepository;
import domain.vo.IntervaloTempo;

import java.util.UUID;

public class CriarEventoUseCase {

    private final EventoRepository eventoRepository;

    public CriarEventoUseCase(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public Evento executar(String titulo, String descricao, IntervaloTempo periodo, String local) {
        Evento novoEvento = new Evento(UUID.randomUUID(), titulo, descricao, periodo, local);
        eventoRepository.salvar(novoEvento);
        return novoEvento;
    }
}