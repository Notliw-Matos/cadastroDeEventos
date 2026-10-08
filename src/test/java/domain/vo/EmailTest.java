package domain.vo;

import domain.exception.ValorInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    void normalizaCaixaEEspacos() {
        assertEquals("ana@exemplo.com", new Email("  ANA@Exemplo.COM ").getEndereco());
    }

    @Test
    void emailsComMesmoEnderecoSaoIguais() {
        assertEquals(new Email("ana@exemplo.com"), new Email("ANA@exemplo.com"));
    }

    @Test
    void aceitaAcentosComoNoSeedDeDemonstracao() {
        assertDoesNotThrow(() -> new Email("otávio.cardoso.oliveira.5@exemplo-organizador.com"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "semarroba.com", "a@b", "a@@b.com", "@exemplo.com", "ana@.com", "ana silva@exemplo.com"})
    void rejeitaEmailsInvalidos(String invalido) {
        assertThrows(ValorInvalidoException.class, () -> new Email(invalido));
    }
}
