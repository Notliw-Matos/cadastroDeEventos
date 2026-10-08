package application;

import domain.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.HasherDeTeste;
import infrastructure.memoria.UsuarioRepositoryEmMemoria;

import static org.junit.jupiter.api.Assertions.*;

/** RF-02: login. Testado só com portas em memória. */
class AutenticarUsuarioUseCaseTest {
    private AutenticarUsuarioUseCase login;

    @BeforeEach
    void preparar() {
        UsuarioRepositoryEmMemoria repositorio = new UsuarioRepositoryEmMemoria();
        HasherDeTeste hasher = new HasherDeTeste();
        new CadastrarUsuarioUseCase(repositorio, hasher).executar("Ana Souza", "ana@exemplo.com", "Senha123");
        login = new AutenticarUsuarioUseCase(repositorio, hasher);
    }

    @Test
    void credenciaisValidasIniciamSessao() {
        UsuarioResumo resumo = login.executar("ana@exemplo.com", "Senha123");
        assertEquals("Ana Souza", resumo.getNome());
    }

    @Test
    void emailNaoDependeDeCaixa() {
        assertDoesNotThrow(() -> login.executar("ANA@exemplo.com", "Senha123"));
    }

    @Test
    void senhaErradaEhRejeitada() {
        assertThrows(CredenciaisInvalidasException.class, () -> login.executar("ana@exemplo.com", "Errada123"));
    }

    @Test
    void emailInexistenteEhRejeitado() {
        assertThrows(CredenciaisInvalidasException.class, () -> login.executar("nao@existe.com", "Senha123"));
    }

    @Test
    void mensagemNaoRevelaSeOEmailExiste() {
        String paraEmailInexistente = assertThrows(CredenciaisInvalidasException.class,
                () -> login.executar("nao@existe.com", "Senha123")).getMessage();
        String paraSenhaErrada = assertThrows(CredenciaisInvalidasException.class,
                () -> login.executar("ana@exemplo.com", "Errada123")).getMessage();
        assertEquals(paraEmailInexistente, paraSenhaErrada);
    }

    @Test
    void entradasNulasOuMalformadasNaoQuebram() {
        assertThrows(CredenciaisInvalidasException.class, () -> login.executar(null, null));
        assertThrows(CredenciaisInvalidasException.class, () -> login.executar("lixo", "Senha123"));
        assertThrows(CredenciaisInvalidasException.class, () -> login.executar("ana@exemplo.com", null));
    }
}
