package domain.model;

import domain.vo.IntervaloTempo;
import java.util.UUID;

public class Atividade {
    public enum TipoAtividade { PALESTRA, OFICINA, MESA_REDONDA, APRESENTACAO_ORAL }

    private final UUID id;
    private final UUID eventoId;
    private String titulo;
    private TipoAtividade tipo;
    private IntervaloTempo intervalo;
    private String local;
    private int capacidadeMaxima;
    private int totalInscritos;

    public Atividade(UUID id, UUID eventoId, String titulo, TipoAtividade tipo, 
                     IntervaloTempo intervalo, String local, int capacidadeMaxima) {
        if (titulo == null || titulo.isEmpty()) throw new IllegalArgumentException("Título é obrigatório.");
        if (capacidadeMaxima <= 0) throw new IllegalArgumentException("Capacidade deve ser maior que zero.");

        this.id = id != null ? id : UUID.randomUUID();
        this.eventoId = eventoId;
        this.titulo = titulo;
        this.tipo = tipo;
        this.intervalo = intervalo;
        this.local = local;
        this.capacidadeMaxima = capacidadeMaxima;
        this.totalInscritos = 0;
    }

    public boolean temVagaDisponivel() {
        return totalInscritos < capacidadeMaxima;
    }

    public void incrementarInscrito() {
        if (!temVagaDisponivel()) {
            throw new IllegalStateException("Limite de vagas atingido para esta atividade.");
        }
        this.totalInscritos++;
    }

    public void decrementarInscrito() {
        if (this.totalInscritos > 0) {
            this.totalInscritos--;
        }
    }

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public String getTitulo() { return titulo; }
    public TipoAtividade getTipo() { return tipo; }
    public IntervaloTempo getIntervalo() { return intervalo; }
    public String getLocal() { return local; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public int getTotalInscritos() { return totalInscritos; }
}