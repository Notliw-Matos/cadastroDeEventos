package domain.model.avaliacao;

import domain.exception.ValorInvalidoException;

import java.util.UUID;

/** Uma resposta de um participante a uma pergunta. Imutável após criada. */
public final class Resposta {
    private final UUID id;
    private final UUID perguntaId;
    private final String valor;

    public Resposta(UUID id, UUID perguntaId, String valor) {
        if (perguntaId == null) throw new ValorInvalidoException("Id da pergunta é obrigatório.");
        if (valor == null || valor.trim().isEmpty()) throw new ValorInvalidoException("Resposta não pode ser vazia.");
        this.id = id != null ? id : UUID.randomUUID();
        this.perguntaId = perguntaId;
        this.valor = valor.trim();
    }

    public UUID getId() { return id; }
    public UUID getPerguntaId() { return perguntaId; }
    public String getValor() { return valor; }
}
