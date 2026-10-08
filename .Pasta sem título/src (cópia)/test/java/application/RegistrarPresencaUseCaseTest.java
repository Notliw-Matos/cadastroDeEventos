package application;

import domain.exception.AcessoNegadoException;
import domain.exception.EstadoInvalidoException;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Atividade;
import domain.model.CriterioFrequencia;
import domain.model.Evento;
import domain.model.Inscricao;
import domain.model.RegistroPresenca.TipoMarcacao;
import domain.model.Usuario;
import domain.vo.Vagas;
import infrastructure.memoria.AtividadeRepositoryEmMemoria;
import infrastructure.memoria.EventoRepositoryEmMemoria;
import infrastructure.memoria.InscricaoRepositoryEmMemoria;
import infrastructure.memoria.RegistroFrequenciaRepositoryEmMemoria;
import infrastructure.memoria.UsuarioRepositoryEmMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Fabrica;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S6. O critério de frequência agora é o da ATIVIDADE de verdade (o caso de uso resolve sozinho
 * qual estratégia usar — RF-19/ROO-05), e todo registro passa pela mesma checagem de autorização
 * de S4 (RN-18: só quem gerencia o evento, ou administrador).
 */
class RegistrarPresencaUseCaseTest {
    private UsuarioRepositoryEmMemoria usuarios;
    private EventoRepositoryEmMemoria eventos;
    private AtividadeRepositoryEmMemoria atividades;
    private InscricaoRepositoryEmMemoria inscricoes;
    private RegistroFrequenciaRepositoryEmMemoria registros;
    private RegistrarPresencaUseCase casoDeUso;

    private Usuario organizador;
    private Usuario outroOrganizador;
    private Evento evento;
    private Inscricao inscricao;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryEmMemoria();
        eventos = new EventoRepositoryEmMemoria();
        atividades = new AtividadeRepositoryEmMemoria();
        inscricoes = new InscricaoRepositoryEmMemoria();
        registros = new RegistroFrequenciaRepositoryEmMemoria();
        casoDeUso = new RegistrarPresencaUseCase(inscricoes, atividades, eventos, usuarios, registros);

        organizador = Fabrica.usuario("dono@exemplo.com", Usuario.Perfil.ORGANIZADOR);
        outroOrganizador = Fabrica.usuario("outro@exemplo.com", Usuario.Perfil.ORGANIZADOR);
        usuarios.salvar(organizador);
        usuarios.salvar(outroOrganizador);

        evento = Evento.novo(organizador.getId(), "Evento", null, Fabrica.intervalo(1, 8, 18), "Sala");
        eventos.salvar(evento);

        inscricao = new Inscricao(null, UUID.randomUUID(), UUID.randomUUID());
        inscricoes.salvar(inscricao);
    }

    private void comAtividadeDeCriterio(CriterioFrequencia criterio) {
        Atividade a = new Atividade(null, evento.getId(), "Atividade", Atividade.TipoAtividade.OFICINA, null, null,
                Fabrica.intervalo(1, 9, 11), Vagas.ilimitadas(), criterio, 0);
        // a inscrição criada em preparar() aponta para um atividadeId aleatório — recriamos apontando pra esta
        inscricao = new Inscricao(inscricao.getId(), inscricao.getParticipanteId(), a.getId());
        inscricoes.salvar(inscricao);
        atividades.salvar(a);
    }

    @Test
    void semMarcacaoNaoHaPresenca() {
        comAtividadeDeCriterio(CriterioFrequencia.CHECK_IN_UNICO);
        assertFalse(casoDeUso.calcularSituacaoDePresenca(inscricao.getId()));
    }

    @Test
    void checkInUnicoValidaComUmCheckIn() {
        comAtividadeDeCriterio(CriterioFrequencia.CHECK_IN_UNICO);
        casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.CHECK_IN, organizador.getId());
        assertTrue(casoDeUso.calcularSituacaoDePresenca(inscricao.getId()));
    }

    @Test
    void entradaESaidaExigeAsDuasMarcacoes() {
        comAtividadeDeCriterio(CriterioFrequencia.ENTRADA_SAIDA);
        casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.CHECK_IN, organizador.getId());
        assertFalse(casoDeUso.calcularSituacaoDePresenca(inscricao.getId()));

        casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.CHECK_OUT, organizador.getId());
        assertTrue(casoDeUso.calcularSituacaoDePresenca(inscricao.getId()));
    }

    @Test
    void criterioManualIgnoraCheckInEAceitaSoLancamentoManual() {
        comAtividadeDeCriterio(CriterioFrequencia.MANUAL);
        casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.CHECK_IN, organizador.getId());
        assertFalse(casoDeUso.calcularSituacaoDePresenca(inscricao.getId()));

        casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.MANUAL, organizador.getId());
        assertTrue(casoDeUso.calcularSituacaoDePresenca(inscricao.getId()));
    }

    @Test
    void registroFicaPersistidoNoRepositorio() {
        comAtividadeDeCriterio(CriterioFrequencia.CHECK_IN_UNICO);
        casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.CHECK_IN, organizador.getId());
        assertEquals(1, registros.listarPorInscricao(inscricao.getId()).size());
        assertEquals(organizador.getId(), registros.listarPorInscricao(inscricao.getId()).get(0).getOperadorId());
    }

    @Test
    void inscricaoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> casoDeUso.registrarMarcacao(UUID.randomUUID(), TipoMarcacao.CHECK_IN, organizador.getId()));
    }

    @Test
    void inscricaoCanceladaNaoRegistraPresenca() {
        comAtividadeDeCriterio(CriterioFrequencia.CHECK_IN_UNICO);
        inscricao.cancelar();
        assertThrows(EstadoInvalidoException.class,
                () -> casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.CHECK_IN, organizador.getId()));
    }

    // ---------------- RN-18: autorização ----------------

    @Test
    void outroOrganizadorNaoRegistraPresencaEmEventoAlheio() {
        comAtividadeDeCriterio(CriterioFrequencia.CHECK_IN_UNICO);
        assertThrows(AcessoNegadoException.class,
                () -> casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.CHECK_IN, outroOrganizador.getId()));
        assertTrue(registros.listarPorInscricao(inscricao.getId()).isEmpty());
    }

    @Test
    void administradorRegistraPresencaEmQualquerEvento() {
        comAtividadeDeCriterio(CriterioFrequencia.CHECK_IN_UNICO);
        Usuario admin = Fabrica.usuario("admin@exemplo.com", Usuario.Perfil.ADMINISTRADOR);
        usuarios.salvar(admin);
        assertDoesNotThrow(() -> casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.CHECK_IN, admin.getId()));
    }

    @Test
    void participanteNaoRegistraPresenca() {
        comAtividadeDeCriterio(CriterioFrequencia.CHECK_IN_UNICO);
        Usuario participante = Fabrica.usuario("part@exemplo.com", Usuario.Perfil.PARTICIPANTE);
        usuarios.salvar(participante);
        assertThrows(AcessoNegadoException.class,
                () -> casoDeUso.registrarMarcacao(inscricao.getId(), TipoMarcacao.CHECK_IN, participante.getId()));
    }
}
