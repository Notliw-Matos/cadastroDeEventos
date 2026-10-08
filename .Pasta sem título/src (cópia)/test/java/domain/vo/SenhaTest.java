package domain.vo;

import domain.exception.ValorInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SenhaTest {

    @Test
    void aceitaSenhaComLetrasENumeros() {
        assertEquals("Demo@123", Senha.de("Demo@123").valor());
    }

    @Test
    void rejeitaSenhaCurta() {
        assertThrows(ValorInvalidoException.class, () -> Senha.de("Ab1"));
    }

    @Test
    void rejeitaSenhaSoComLetras() {
        assertThrows(ValorInvalidoException.class, () -> Senha.de("somenteletras"));
    }

    @Test
    void rejeitaSenhaSoComNumeros() {
        assertThrows(ValorInvalidoException.class, () -> Senha.de("123456789"));
    }

    @Test
    void rejeitaSenhaNula() {
        assertThrows(ValorInvalidoException.class, () -> Senha.de(null));
    }

    @Test
    void rejeitaSenhaMaiorQueOLimiteDoBcrypt() {
        StringBuilder longa = new StringBuilder("a1");
        while (longa.length() <= Senha.MAXIMO) longa.append('x');
        assertThrows(ValorInvalidoException.class, () -> Senha.de(longa.toString()));
    }

    @Test
    void naoVazaSenhaNoToString() {
        assertFalse(Senha.de("Demo@123").toString().contains("Demo"));
    }
}
