package application;

import domain.exception.AcessoNegadoException;
import domain.exception.EstadoInvalidoException;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Atividade;
import domain.model.Evento;
import domain.model.Usuario;
import domain.vo.Vagas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import infrastructure.memoria.AtividadeRepositoryEmMemoria;
import infrastructure.memoria.EventoRepositoryEmMemoria;
import support.Fabrica;
import infrastructure.memoria.UsuarioRepositoryEmMemoria;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CriarAtividadeUseCaseTest {
    private AtividadeRepositoryEmMemoria atividades;
    private CriarAtividadeUseCase casoDeUso;
    private Usuario dono;
    private Usuario outroOrganizador;
    private Usuario admin;
    private Evento evento;

    @BeforeEach
    void preparar() {
        UsuarioRepositoryEmMemoria usuarios = new UsuarioRepositoryEmMemoria();
        EventoRepositoryEmMemoria eventos = new EventoRepositoryEmMemoria();
        atividades = new AtividadeRepositoryEmMemoria();
        casoDeUso = new CriarAtividadeUseCase(atividades, eventos, usuarios);

        dono = Fabrica.usuario("dono@exemplo.com", Usuario.Perfil.ORGANIZADOR);
        outroOrganizador = Fabrica.usuario("outro@exemplo.com", Usuario.Perfil.ORGANIZADOR);
        admin = Fabrica.usuario("admin@exemplo.com", Usuario.Perfil.ADMINISTRADOR);
        usuarios.salvar(dono);
        usuarios.salvar(outroOrganizador);
        usuarios.salvar(admin);

        evento = Evento.novo(dono.getId(), "Simpósio", null, Fabrica.intervalo(10, 8, 18), "Auditório");
        eventos.salvar(evento);
    }

    private Atividade criarComo(Usuario quem) {
        return casoDeUso.executar(quem.getId(), evento.getId(), "Oficina POO",
                Atividade.TipoAtividade.OFICINA, Fabrica.intervalo(10, 9, 11), Vagas.limitadas(30));
    }

    @Test
    void donoDoEventoCriaAtividade() {
        Atividade criada = criarComo(dono);
        assertEquals(evento.getId(), criada.getEventoId());
        assertTrue(atividades.buscarPorId(criada.getId()).isPresent());
    }

    @Test
    void administradorCriaAtividadeEmQualquerEvento() {
        assertNotNull(criarComo(admin));
    }

    @Test
    void outroOrganizadorNaoMexeNoEventoAlheio() {
        assertThrows(AcessoNegadoException.class, () -> criarComo(outroOrganizador));
        assertTrue(atividades.listarPorEvento(evento.getId()).isEmpty());
    }

    @Test
    void eventoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> casoDeUso.executar(dono.getId(), UUID.randomUUID(),
                "x", Atividade.TipoAtividade.PALESTRA, Fabrica.intervalo(1, 9, 10), Vagas.ilimitadas()));
    }

    @Test
    void eventoEncerradoNaoRecebeAtividades() {
        evento.publicar();
        evento.encerrar();
        assertThrows(EstadoInvalidoException.class, () -> criarComo(dono));
    }
}
