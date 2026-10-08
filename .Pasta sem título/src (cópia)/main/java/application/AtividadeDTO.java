package application;

import domain.model.Atividade;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class AtividadeDTO {
    private final UUID id;
    private final String titulo;
    private final String tipo;
    private final String inicio;
    private final String fim;
    private final boolean temVaga;

    private AtividadeDTO(UUID id, String titulo, String tipo, String inicio, String fim, boolean temVaga) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
        this.inicio = inicio;
        this.fim = fim;
        this.temVaga = temVaga;
    }

    public static AtividadeDTO de(Atividade atividade) {
        return new AtividadeDTO(atividade.getId(), atividade.getTitulo(), atividade.getTipo().name(),
                atividade.getIntervalo().getInicio().toString(), atividade.getIntervalo().getFim().toString(),
                atividade.temVagaDisponivel());
    }

    public Map<String, Object> paraMapa() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", id.toString());
        mapa.put("titulo", titulo);
        mapa.put("tipo", tipo);
        mapa.put("inicio", inicio);
        mapa.put("fim", fim);
        mapa.put("temVaga", temVaga);
        return mapa;
    }
}
