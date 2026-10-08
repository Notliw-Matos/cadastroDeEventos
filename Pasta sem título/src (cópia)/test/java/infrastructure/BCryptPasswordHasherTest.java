package infrastructure;

import infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordHasherTest {
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    /** Hash gerado pelo pgcrypto (crypt('Demo@123', gen_salt('bf'))) do 02_seed_usuarios.sql. */
    private static final String HASH_DO_SEED = "$2a$06$dJq2uQq2G4aDYqQGI0fVnO82vS.puHfl7i8MQwjn/MR0B7EzNrN/O";

    @Test
    void hashNaoContemASenhaEConfereComEla() {
        String hash = hasher.hash("Senha123");
        assertFalse(hash.contains("Senha123"));
        assertTrue(hasher.confere("Senha123", hash));
    }

    @Test
    void senhaDiferenteNaoConfere() {
        assertFalse(hasher.confere("Outra1234", hasher.hash("Senha123")));
    }

    @Test
    void mesmaSenhaGeraHashesDiferentesPorCausaDoSalt() {
        assertNotEquals(hasher.hash("Senha123"), hasher.hash("Senha123"));
    }

    @Test
    void confereComHashGeradoPeloPgcryptoDoSeed() {
        assertTrue(hasher.confere("Demo@123", HASH_DO_SEED));
        assertFalse(hasher.confere("demo@123", HASH_DO_SEED));
    }

    @Test
    void hashMalformadoOuNuloNuncaAutentica() {
        assertFalse(hasher.confere("Senha123", "isto-nao-e-um-hash"));
        assertFalse(hasher.confere("Senha123", null));
        assertFalse(hasher.confere(null, HASH_DO_SEED));
    }
}
