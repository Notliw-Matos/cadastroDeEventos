package domain.model.avaliacao;

import domain.exception.ValorInvalidoException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Uma pergunta de um questionário (RF-24, RF-25). Imutável — criar outra para alterar. */
public final class Pergunta {
    private final UUID id;
    private final String enunciado;
    private final TipoPergunta tipo;
    private final List<String> opcoes; // só preenchida para ESCOLHA_UNICA

    public Pergunta(UUID id, String enunciado, TipoPergunta tipo, List<String> opcoes) {
        if (enunciado == null || enunciado.trim().isEmpty())
            throw new ValorInvalidoException("Enunciado da pergunta é obrigatório.");
        if (tipo == null)
            throw new ValorInvalidoException("Tipo da pergunta é obrigatório.");
        if (tipo == TipoPergunta.ESCOLHA_UNICA && (opcoes == null || opcoes.size() < 2))
            throw new ValorInvalidoException("Pergunta de escolha única precisa de pelo menos 2 opções.");
        this.id = id != null ? id : UUID.randomUUID();
        this.enunciado = enunciado.trim();
        this.tipo = tipo;
        this.opcoes = opcoes != null ? Collections.unmodifiableList(opcoes) : Collections.emptyList();
    }

    /** Valida a resposta delegando ao tipo — sem if/switch no chamador (ROO-05). */
    public void validarResposta(String valor) {
        tipo.validarResposta(valor);
        if (tipo == TipoPergunta.ESCOLHA_UNICA && !opcoes.contains(valor)) {
            throw new ValorInvalidoException("Opção \"" + valor + "\" não existe nesta pergunta.");
        }
    }

    public UUID getId() { return id; }
    public String getEnunciado() { return enunciado; }
    public TipoPergunta getTipo() { return tipo; }
    public List<String> getOpcoes() { return opcoes; }
}
