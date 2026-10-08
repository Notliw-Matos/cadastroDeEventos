package domain.model;

import domain.exception.AcessoNegadoException;
import domain.exception.ValorInvalidoException;
import domain.vo.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {
    private final Email email = new Email("ana@exemplo.com");

    @Test
    void cadastroPublicoSempreGeraParticipante() {
        Usuario u = Usuario.novoParticipante("Ana", email, "hash");
        assertEquals(Usuario.Perfil.PARTICIPANTE, u.getPerfil());
    }

    @Test
    void participanteNaoGerenciaEventos() {
        Usuario u = Usuario.novoParticipante("Ana", email, "hash");
        assertFalse(u.podeGerenciarEventos());
        assertThrows(AcessoNegadoException.class, u::exigirPermissaoParaGerenciarEventos);
    }

    @Test
    void organizadorEAdministradorGerenciamEventos() {
        assertTrue(new Usuario(null, "O", email, "h", Usuario.Perfil.ORGANIZADOR).podeGerenciarEventos());
        assertTrue(new Usuario(null, "A", email, "h", Usuario.Perfil.ADMINISTRADOR).podeGerenciarEventos());
    }

    @Test
    void somenteAdministradorEAdministrador() {
        assertTrue(new Usuario(null, "A", email, "h", Usuario.Perfil.ADMINISTRADOR).isAdministrador());
        assertFalse(new Usuario(null, "O", email, "h", Usuario.Perfil.ORGANIZADOR).isAdministrador());
    }

    @Test
    void camposObrigatorios() {
        assertThrows(ValorInvalidoException.class, () -> Usuario.novoParticipante(" ", email, "h"));
        assertThrows(ValorInvalidoException.class, () -> Usuario.novoParticipante("Ana", null, "h"));
        assertThrows(ValorInvalidoException.class, () -> Usuario.novoParticipante("Ana", email, ""));
        assertThrows(ValorInvalidoException.class, () -> new Usuario(null, "Ana", email, "h", null));
    }
}
