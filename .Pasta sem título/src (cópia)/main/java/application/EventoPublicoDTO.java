package application;

import domain.model.Evento;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DTO de saída (S4: "separação entre DTOs, aplicação e domínio"). O site público
 * só recebe estes campos: nunca o organizadorId nem detalhes internos do Evento.
 */
public final class EventoPublicoDTO {
    private final UUID id;
    private final String titulo;
    private final String descricao;
    private final String inicio;
    private final String fim;
    private final String local;

    private EventoPublicoDTO(UUID id, String titulo, String descricao, String inicio, String fim, String local) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.inicio = inicio;
        this.fim = fim;
        this.local = local;
    }

    public static EventoPublicoDTO de(Evento evento) {
        return new EventoPublicoDTO(evento.getId(), evento.getTitulo(), evento.getDescricao(),
                evento.getPeriodo().getInicio().toString(), evento.getPeriodo().getFim().toString(),
                evento.getLocalOuModalidade());
    }

    public Map<String, Object> paraMapa() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", id.toString());
        mapa.put("titulo", titulo);
        mapa.put("descricao", descricao);
        mapa.put("inicio", inicio);
        mapa.put("fim", fim);
        mapa.put("local", local);
        return mapa;
    }
}
