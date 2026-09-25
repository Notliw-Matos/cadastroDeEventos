package domain.model;

import domain.vo.IntervaloTempo;
import java.util.Objects;
import java.util.UUID;

public class Evento {
    private final UUID id;
    private String titulo;
    private String descricao;
    private IntervaloTempo periodo;
    private String local;

    public Evento(UUID id, String titulo, String descricao, IntervaloTempo periodo, String local) {
        this.id = Objects.requireNonNull(id, "ID não pode ser nulo.");
        this.titulo = Objects.requireNonNull(titulo, "Título não pode ser nulo.");
        this.descricao = descricao;
        this.periodo = Objects.requireNonNull(periodo, "Período não pode ser nulo.");
        this.local = local;
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public IntervaloTempo getPeriodo() { return periodo; }
    public String getLocal() { return local; }
}