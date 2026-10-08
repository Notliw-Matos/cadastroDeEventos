package domain.model;

import domain.exception.AcessoNegadoException;
import domain.exception.EstadoInvalidoException;
import domain.exception.ValorInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Fabrica;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventoTest {
    private Usuario organizador;
    private Evento evento;

    @BeforeEach
    void preparar() {
        organizador = Fabrica.usuario("org@exemplo.com", Usuario.Perfil.ORGANIZADOR);
        evento = Evento.novo(organizador.getId(), "Simpósio", "Descrição", Fabrica.intervalo(10, 8, 18), "Auditório");
    }

    @Test
    void nasceEmRascunhoENaoEVisivelAoPublico() {
        assertEquals(Evento.Estado.RASCUNHO, evento.getEstado());
        assertFalse(evento.isVisivelAoPublico());
        assertFalse(evento.aceitaInscricoes());
    }

    @Test
    void publicarTornaVisivelEAbreInscricoes() {
        evento.publicar();
        assertTrue(evento.isVisivelAoPublico());
        assertTrue(evento.aceitaInscricoes());
    }

    @Test
    void naoPodeSerEncerradoSemTerSidoPublicado() {
        assertThrows(EstadoInvalidoException.class, evento::encerrar);
    }

    @Test
    void eventoEncerradoNaoPodeSerAlteradoNemRepublicado() {
        evento.publicar();
        evento.encerrar();
        assertThrows(EstadoInvalidoException.class, evento::publicar);
        assertThrows(EstadoInvalidoException.class,
                () -> evento.alterarDados("Novo", null, Fabrica.intervalo(1, 8, 9), "Sala"));
        assertFalse(evento.aceitaInscricoes());
        assertFalse(evento.aceitaNovasAtividades());
    }

    @Test
    void tituloEObrigatorio() {
        assertThrows(ValorInvalidoException.class,
                () -> Evento.novo(organizador.getId(), "  ", null, Fabrica.intervalo(1, 8, 9), "x"));
    }

    @Test
    void alteracaoInvalidaNaoCorrompeOEstadoAnterior() {
        assertThrows(ValorInvalidoException.class, () -> evento.alterarDados("", "d", Fabrica.intervalo(1, 8, 9), "x"));
        assertEquals("Simpósio", evento.getTitulo());
    }

    @Test
    void organizadorGerenciaApenasOSeuEvento() {
        Usuario outroOrganizador = Fabrica.usuario("outro@exemplo.com", Usuario.Perfil.ORGANIZADOR);
        assertTrue(evento.podeSerGerenciadoPor(organizador));
        assertFalse(evento.podeSerGerenciadoPor(outroOrganizador));
        assertThrows(AcessoNegadoException.class, () -> evento.exigirGerenciadoPor(outroOrganizador));
    }

    @Test
    void administradorGerenciaQualquerEvento() {
        Usuario admin = Fabrica.usuario("admin@exemplo.com", Usuario.Perfil.ADMINISTRADOR);
        assertTrue(evento.podeSerGerenciadoPor(admin));
    }

    @Test
    void participanteNuncaGerenciaEvento() {
        Usuario participante = Fabrica.usuario("p@exemplo.com", Usuario.Perfil.PARTICIPANTE);
        assertFalse(evento.podeSerGerenciadoPor(participante));
        assertFalse(evento.podeSerGerenciadoPor(null));
    }

    @Test
    void fusoPadraoEHorarioDeSaoPaulo() {
        assertEquals("America/Sao_Paulo", evento.getFusoHorario().getId());
    }

    @Test
    void reconstituirExigeOrganizador() {
        assertThrows(NullPointerException.class, () -> new Evento(UUID.randomUUID(), "t", null,
                Fabrica.intervalo(1, 8, 9), "l", Evento.Estado.RASCUNHO, Evento.FUSO_PADRAO, null));
    }
}
