package application;

import domain.exception.AcessoNegadoException;
import domain.exception.RecursoNaoEncontradoException;
import domain.exception.ValorInvalidoException;
import domain.model.Evento;
import domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import infrastructure.memoria.EventoRepositoryEmMemoria;
import support.Fabrica;
import infrastructure.memoria.UsuarioRepositoryEmMemoria;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** RF-04 + RNF-06 / RN-18: a autorização é imposta no caso de uso, não na tela. */
class CriarEventoUseCaseTest {
    private UsuarioRepositoryEmMemoria usuarios;
    private EventoRepositoryEmMemoria eventos;
    private CriarEventoUseCase casoDeUso;
    private Usuario organizador;
    private Usuario participante;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryEmMemoria();
        eventos = new EventoRepositoryEmMemoria();
        casoDeUso = new CriarEventoUseCase(eventos, usuarios);
        organizador = Fabrica.usuario("org@exemplo.com", Usuario.Perfil.ORGANIZADOR);
        participante = Fabrica.usuario("part@exemplo.com", Usuario.Perfil.PARTICIPANTE);
        usuarios.salvar(organizador);
        usuarios.salvar(participante);
    }

    @Test
    void organizadorCriaEventoEmRascunhoSendoODono() {
        Evento evento = casoDeUso.executar(organizador.getId(), "Simpósio", "desc", Fabrica.intervalo(10, 8, 18), "Auditório");

        assertEquals(Evento.Estado.RASCUNHO, evento.getEstado());
        assertEquals(organizador.getId(), evento.getOrganizadorId());
        assertTrue(eventos.buscarPorId(evento.getId()).isPresent());
    }

    @Test
    void participanteNaoCriaEventoENadaEPersistido() {
        assertThrows(AcessoNegadoException.class,
                () -> casoDeUso.executar(participante.getId(), "Simpósio", null, Fabrica.intervalo(10, 8, 18), "x"));
        assertEquals(0, eventos.total());
    }

    @Test
    void usuarioInexistenteNaoCriaEvento() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> casoDeUso.executar(UUID.randomUUID(), "Simpósio", null, Fabrica.intervalo(10, 8, 18), "x"));
    }

    @Test
    void dadosInvalidosNaoPersistem() {
        assertThrows(ValorInvalidoException.class,
                () -> casoDeUso.executar(organizador.getId(), "", null, Fabrica.intervalo(10, 8, 18), "x"));
        assertEquals(0, eventos.total());
    }
}
