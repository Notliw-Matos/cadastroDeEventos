package domain.ports;

import domain.model.Evento;
import java.util.Optional;
import java.util.UUID;

public interface EventoRepository {
    void salvar(Evento evento);
    Optional<Evento> buscarPorId(UUID id);
}