package domain.model.avaliacao;

import domain.exception.ValorInvalidoException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Questionário de uma atividade (RF-24). Criado pelo organizador, respondido por participantes elegíveis. */
public final class Questionario {
    private final UUID id;
    private final UUID atividadeId;
    private final String titulo;
    private final List<Pergunta> perguntas;

    public Questionario(UUID id, UUID atividadeId, String titulo, List<Pergunta> perguntas) {
        if (atividadeId == null) throw new ValorInvalidoException("Questionário deve estar associado a uma atividade.");
        if (titulo == null || titulo.trim().isEmpty()) throw new ValorInvalidoException("Título do questionário é obrigatório.");
        if (perguntas == null || perguntas.isEmpty()) throw new ValorInvalidoException("Questionário precisa de pelo menos uma pergunta.");
        this.id = id != null ? id : UUID.randomUUID();
        this.atividadeId = atividadeId;
        this.titulo = titulo.trim();
        this.perguntas = Collections.unmodifiableList(perguntas);
    }

    public UUID getId() { return id; }
    public UUID getAtividadeId() { return atividadeId; }
    public String getTitulo() { return titulo; }
    public List<Pergunta> getPerguntas() { return perguntas; }
}
