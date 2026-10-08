package infrastructure.memoria;

import domain.model.Evento;
import domain.ports.EventoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EventoRepositoryEmMemoria implements EventoRepository {
    private final Map<UUID, Evento> porId = new LinkedHashMap<>();

    @Override
    public void salvar(Evento evento) {
        porId.put(evento.getId(), evento);
    }

    @Override
    public Optional<Evento> buscarPorId(UUID id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<Evento> listarPublicados() {
        List<Evento> resultado = new ArrayList<>();
        for (Evento e : porId.values()) {
            if (e.isVisivelAoPublico()) resultado.add(e);
        }
        return resultado;
    }

    public int total() {
        return porId.size();
    }
}
