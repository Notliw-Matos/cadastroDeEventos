package domain.model.avaliacao;

import domain.exception.ValorInvalidoException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AvaliacaoTest {

    private Pergunta textoPergunta() {
        return new Pergunta(null, "O que achou?", TipoPergunta.TEXTO_LIVRE, null);
    }
    private Pergunta escalaPergunta() {
        return new Pergunta(null, "Nota de 1 a 10:", TipoPergunta.ESCALA_NUMERICA, null);
    }
    private Pergunta escolhaPergunta() {
        return new Pergunta(null, "Recomendaria?", TipoPergunta.ESCOLHA_UNICA, Arrays.asList("Sim","Nao"));
    }

    @Test void enunciadoObrigatorio() {
        assertThrows(ValorInvalidoException.class, () -> new Pergunta(null, "  ", TipoPergunta.TEXTO_LIVRE, null));
    }
    @Test void escolhaUnicaExigePeloMenosDuasOpcoes() {
        assertThrows(ValorInvalidoException.class, () -> new Pergunta(null, "x", TipoPergunta.ESCOLHA_UNICA, List.of("so uma")));
    }
    @Test void textoLivreAceitaQualquerTexto() {
        assertDoesNotThrow(() -> textoPergunta().validarResposta("Gostei muito!"));
    }
    @Test void textoLivreRejeitaVazio() {
        assertThrows(ValorInvalidoException.class, () -> textoPergunta().validarResposta("  "));
    }
    @Test void escalaAceitaIntreiroEntreUmEDez() {
        assertDoesNotThrow(() -> escalaPergunta().validarResposta("7"));
    }
    @Test void escalaRejeitaForaDoIntervalo() {
        assertThrows(ValorInvalidoException.class, () -> escalaPergunta().validarResposta("11"));
        assertThrows(ValorInvalidoException.class, () -> escalaPergunta().validarResposta("0"));
    }
    @Test void escalaRejeitaTextoNaoNumerico() {
        assertThrows(ValorInvalidoException.class, () -> escalaPergunta().validarResposta("bom"));
    }
    @Test void escolhaAceitaOpcaoValida() {
        assertDoesNotThrow(() -> escolhaPergunta().validarResposta("Sim"));
    }
    @Test void escolhaRejeitaOpcaoInexistente() {
        assertThrows(ValorInvalidoException.class, () -> escolhaPergunta().validarResposta("Talvez"));
    }
    @Test void questionarioSemPerguntasEhInvalido() {
        assertThrows(ValorInvalidoException.class,
                () -> new Questionario(null, UUID.randomUUID(), "Q", List.of()));
    }
    @Test void criarRespostaValidaTodasAsPerguntas() {
        Pergunta p1 = textoPergunta(), p2 = escalaPergunta(), p3 = escolhaPergunta();
        Questionario q = new Questionario(null, UUID.randomUUID(), "Q", List.of(p1, p2, p3));
        RespostaQuestionario r = RespostaQuestionario.criar(null, q, UUID.randomUUID(),
                Map.of(p1.getId(), "Otimo", p2.getId(), "8", p3.getId(), "Sim"));
        assertEquals(3, r.getRespostas().size());
    }
    @Test void criarRespostaFalhaSeAlgumaPerguntaNaoForRespondida() {
        Pergunta p1 = textoPergunta(), p2 = escalaPergunta();
        Questionario q = new Questionario(null, UUID.randomUUID(), "Q", List.of(p1, p2));
        assertThrows(ValorInvalidoException.class, () -> RespostaQuestionario.criar(null, q, UUID.randomUUID(),
                Map.of(p1.getId(), "Otimo")));
    }
    @Test void criarRespostaFalhaSeValorInvalidoParaOTipo() {
        Pergunta p1 = escalaPergunta();
        Questionario q = new Questionario(null, UUID.randomUUID(), "Q", List.of(p1));
        assertThrows(ValorInvalidoException.class, () -> RespostaQuestionario.criar(null, q, UUID.randomUUID(),
                Map.of(p1.getId(), "excelente")));
    }
}
