package application;

import domain.exception.EmailJaCadastradoException;
import domain.exception.ValorInvalidoException;
import domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.HasherDeTeste;
import infrastructure.memoria.UsuarioRepositoryEmMemoria;

import static org.junit.jupiter.api.Assertions.*;

/** RF-01 / RN-01 / RNF-05, sem interface e sem banco: só o caso de uso e portas em memória. */
class CadastrarUsuarioUseCaseTest {
    private UsuarioRepositoryEmMemoria repositorio;
    private CadastrarUsuarioUseCase casoDeUso;

    @BeforeEach
    void preparar() {
        repositorio = new UsuarioRepositoryEmMemoria();
        casoDeUso = new CadastrarUsuarioUseCase(repositorio, new HasherDeTeste());
    }

    @Test
    void cadastraParticipanteValido() {
        UsuarioResumo resumo = casoDeUso.executar("Ana Souza", "ana@exemplo.com", "Senha123");

        assertEquals("ana@exemplo.com", resumo.getEmail());
        assertEquals(Usuario.Perfil.PARTICIPANTE, resumo.getPerfil());
        assertEquals(1, repositorio.total());
    }

    @Test
    void senhaNuncaEGuardadaEmTextoPuro() {
        UsuarioResumo resumo = casoDeUso.executar("Ana Souza", "ana@exemplo.com", "Senha123");

        Usuario salvo = repositorio.buscarPorId(resumo.getId()).get();
        assertNotEquals("Senha123", salvo.getSenhaHash());
        assertFalse(salvo.getSenhaHash().contains("Senha123"));
    }

    @Test
    void rejeitaEmailDuplicadoComMensagemClara() {
        casoDeUso.executar("Ana Souza", "ana@exemplo.com", "Senha123");

        EmailJaCadastradoException erro = assertThrows(EmailJaCadastradoException.class,
                () -> casoDeUso.executar("Outra Ana", "ana@exemplo.com", "Outra1234"));
        assertEquals("Este e-mail já está cadastrado.", erro.getMessage());
        assertEquals(1, repositorio.total());
    }

    @Test
    void emailDuplicadoIgnoraCaixaEEspacos() {
        casoDeUso.executar("Ana Souza", "ana@exemplo.com", "Senha123");
        assertThrows(EmailJaCadastradoException.class,
                () -> casoDeUso.executar("Outra Ana", "  ANA@Exemplo.com ", "Outra1234"));
    }

    @Test
    void rejeitaSenhaFracaSemCadastrar() {
        assertThrows(ValorInvalidoException.class, () -> casoDeUso.executar("Ana", "ana@exemplo.com", "123"));
        assertEquals(0, repositorio.total());
    }

    @Test
    void rejeitaEmailInvalidoSemCadastrar() {
        assertThrows(ValorInvalidoException.class, () -> casoDeUso.executar("Ana", "isso-nao-e-email", "Senha123"));
        assertEquals(0, repositorio.total());
    }

    @Test
    void rejeitaNomeEmBrancoSemCadastrar() {
        assertThrows(ValorInvalidoException.class, () -> casoDeUso.executar("   ", "ana@exemplo.com", "Senha123"));
        assertEquals(0, repositorio.total());
    }
}
