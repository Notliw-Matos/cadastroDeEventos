package domain.ports;

import domain.model.Evento;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoRepository {
    void salvar(Evento evento);
    Optional<Evento> buscarPorId(UUID id);

    /** RF-10: só os eventos publicados aparecem no site público. */
    List<Evento> listarPublicados();
}