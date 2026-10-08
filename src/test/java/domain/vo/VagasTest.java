package domain.vo;

import domain.exception.ValorInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VagasTest {

    @Test
    void limitadasExigemLimitePositivo() {
        assertThrows(ValorInvalidoException.class, () -> Vagas.limitadas(0));
        assertThrows(ValorInvalidoException.class, () -> Vagas.limitadas(-3));
    }

    @Test
    void limitadasPermitemAteOLimite() {
        Vagas vagas = Vagas.limitadas(2);
        assertTrue(vagas.haVagaCom(0));
        assertTrue(vagas.haVagaCom(1));
        assertFalse(vagas.haVagaCom(2));
    }

    @Test
    void ilimitadasSempreTemVaga() {
        assertTrue(Vagas.ilimitadas().haVagaCom(1_000_000));
        assertNull(Vagas.ilimitadas().getLimite());
    }

    @Test
    void colunaNulaDoBancoSignificaSemControle() {
        assertEquals(Vagas.ilimitadas(), Vagas.doLimite(null));
        assertEquals(Vagas.limitadas(30), Vagas.doLimite(30));
    }

    @Test
    void comportaValidaTotalReconstituido() {
        assertTrue(Vagas.limitadas(5).comporta(5));
        assertFalse(Vagas.limitadas(5).comporta(6));
    }
}
