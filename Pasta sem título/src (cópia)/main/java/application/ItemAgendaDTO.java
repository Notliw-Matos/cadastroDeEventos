package application;

import domain.model.Atividade;
import domain.model.Inscricao;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class ItemAgendaDTO {
    private final UUID atividadeId;
    private final String titulo;
    private final String tipo;
    private final String inicio;
    private final String fim;
    private final String situacaoInscricao;

    private ItemAgendaDTO(UUID atividadeId, String titulo, String tipo, String inicio, String fim, String situacaoInscricao) {
        this.atividadeId = atividadeId;
        this.titulo = titulo;
        this.tipo = tipo;
        this.inicio = inicio;
        this.fim = fim;
        this.situacaoInscricao = situacaoInscricao;
    }

    public static ItemAgendaDTO de(Atividade atividade, Inscricao inscricao) {
        return new ItemAgendaDTO(atividade.getId(), atividade.getTitulo(), atividade.getTipo().name(),
                atividade.getIntervalo().getInicio().toString(), atividade.getIntervalo().getFim().toString(),
                inscricao.getSituacao().name());
    }

    public String getInicio() {
        return inicio;
    }

    public Map<String, Object> paraMapa() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("atividadeId", atividadeId.toString());
        mapa.put("titulo", titulo);
        mapa.put("tipo", tipo);
        mapa.put("inicio", inicio);
        mapa.put("fim", fim);
        mapa.put("situacaoInscricao", situacaoInscricao);
        return mapa;
    }
}
