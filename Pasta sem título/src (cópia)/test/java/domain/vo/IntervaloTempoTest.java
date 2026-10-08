package domain.vo;

import domain.exception.ValorInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class IntervaloTempoTest {
    private static final LocalDateTime NOVE = LocalDateTime.of(2026, 8, 20, 9, 0);

    private IntervaloTempo das(int inicio, int fim) {
        return new IntervaloTempo(NOVE.withHour(inicio), NOVE.withHour(fim));
    }

    @Test
    void rejeitaFimAnteriorAoInicio() {
        assertThrows(ValorInvalidoException.class, () -> das(11, 9));
    }

    @Test
    void rejeitaIntervaloDeDuracaoZero() {
        assertThrows(ValorInvalidoException.class, () -> das(9, 9));
    }

    @Test
    void rejeitaDatasNulas() {
        assertThrows(ValorInvalidoException.class, () -> new IntervaloTempo(null, NOVE));
        assertThrows(ValorInvalidoException.class, () -> new IntervaloTempo(NOVE, null));
    }

    @Test
    void detectaSobreposicao() {
        assertTrue(das(9, 11).temConflitoCom(das(10, 12)));
        assertTrue(das(10, 12).temConflitoCom(das(9, 11)));
    }

    @Test
    void detectaIntervaloContidoEmOutro() {
        assertTrue(das(9, 17).temConflitoCom(das(10, 11)));
    }

    @Test
    void terminarQuandoOOutroComecaNaoEConflito() {
        assertFalse(das(9, 10).temConflitoCom(das(10, 11)));
    }

    @Test
    void intervalosDistantesNaoConflitam() {
        assertFalse(das(8, 9).temConflitoCom(das(14, 15)));
    }
}
