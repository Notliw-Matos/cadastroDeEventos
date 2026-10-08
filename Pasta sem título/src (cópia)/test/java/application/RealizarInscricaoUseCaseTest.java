package application;

import domain.exception.ConflitoDeHorarioException;
import domain.exception.InscricaoDuplicadaException;
import domain.exception.RecursoNaoEncontradoException;
import domain.exception.VagasEsgotadasException;
import domain.model.Atividade;
import domain.strategy.PoliticaConflitoAlertaSomente;
import domain.vo.Vagas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import infrastructure.memoria.AtividadeRepositoryEmMemoria;
import support.Fabrica;
import infrastructure.memoria.InscricaoRepositoryEmMemoria;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RealizarInscricaoUseCaseTest {
    private final UUID evento = UUID.randomUUID();
    private final UUID ana = UUID.randomUUID();
    private AtividadeRepositoryEmMemoria atividades;
    private InscricaoRepositoryEmMemoria inscricoes;
    private RealizarInscricaoUseCase casoDeUso;

    @BeforeEach
    void preparar() {
        atividades = new AtividadeRepositoryEmMemoria();
        inscricoes = new InscricaoRepositoryEmMemoria();
        casoDeUso = new RealizarInscricaoUseCase(inscricoes, atividades); // política padrão: bloqueante
    }

    private Atividade atividade(String titulo, int inicio, int fim, Vagas vagas, int jaInscritos) {
        Atividade a = new Atividade(null, evento, titulo, Atividade.TipoAtividade.PALESTRA, null, null,
                Fabrica.intervalo(1, inicio, fim), vagas, domain.model.CriterioFrequencia.CHECK_IN_UNICO, jaInscritos);
        atividades.salvar(a);
        return a;
    }

    @Test
    void inscreveParticipante() {
        Atividade a = atividade("Palestra", 9, 10, Vagas.limitadas(10), 0);
        ResultadoInscricao resultado = casoDeUso.executar(ana, a.getId());
        assertTrue(resultado.getInscricao().isAtiva());
        assertNotNull(resultado.getInscricao().getId());
        assertTrue(resultado.getAvisos().isEmpty());
    }

    @Test
    void persisteONovoTotalDeInscritosNaAtividade() {
        Atividade a = atividade("Palestra", 9, 10, Vagas.limitadas(10), 0);
        casoDeUso.executar(ana, a.getId());
        // RN-06 / bug de S4: sem o atividadeRepository.salvar() dentro do caso de uso, isto voltaria 0.
        assertEquals(1, atividades.buscarPorId(a.getId()).get().getTotalInscritos());
    }

    @Test
    void naoInscreveQuandoNaoHaVagas() {
        Atividade cheia = atividade("Cheia", 9, 10, Vagas.limitadas(1), 1);
        assertThrows(VagasEsgotadasException.class, () -> casoDeUso.executar(ana, cheia.getId()));
        assertTrue(inscricoes.listarPorParticipante(ana).isEmpty());
    }

    @Test
    void naoInscreveEmAtividadeComHorarioConflitante() {
        Atividade a = atividade("Manhã A", 9, 11, Vagas.ilimitadas(), 0);
        Atividade b = atividade("Manhã B", 10, 12, Vagas.ilimitadas(), 0);
        casoDeUso.executar(ana, a.getId());

        ConflitoDeHorarioException erro = assertThrows(ConflitoDeHorarioException.class,
                () -> casoDeUso.executar(ana, b.getId()));
        assertTrue(erro.getMessage().contains("Manhã A"));
    }

    @Test
    void atividadesEmSequenciaNaoConflitam() {
        Atividade a = atividade("A", 9, 10, Vagas.ilimitadas(), 0);
        Atividade b = atividade("B", 10, 11, Vagas.ilimitadas(), 0);
        casoDeUso.executar(ana, a.getId());
        assertDoesNotThrow(() -> casoDeUso.executar(ana, b.getId()));
    }

    @Test
    void repetirAOperacaoNaoDuplicaAInscricao() {
        Atividade a = atividade("A", 9, 10, Vagas.ilimitadas(), 0);
        casoDeUso.executar(ana, a.getId());
        assertThrows(InscricaoDuplicadaException.class, () -> casoDeUso.executar(ana, a.getId()));
        assertEquals(1, inscricoes.listarPorParticipante(ana).size());
    }

    @Test
    void inscricaoCanceladaNaoGeraConflito() {
        Atividade a = atividade("A", 9, 11, Vagas.ilimitadas(), 0);
        Atividade b = atividade("B", 10, 12, Vagas.ilimitadas(), 0);
        casoDeUso.executar(ana, a.getId()).getInscricao().cancelar();
        assertDoesNotThrow(() -> casoDeUso.executar(ana, b.getId()));
    }

    @Test
    void atividadeInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> casoDeUso.executar(ana, UUID.randomUUID()));
    }

    // ---------------- S5: política de conflito alternativa (composição + polimorfismo) ----------------

    @Test
    void comPoliticaDeAlertaAmbasAsInscricoesSaoAceitasEGeraAviso() {
        RealizarInscricaoUseCase comAlerta =
                new RealizarInscricaoUseCase(inscricoes, atividades, new PoliticaConflitoAlertaSomente());
        Atividade a = atividade("Manhã A", 9, 11, Vagas.ilimitadas(), 0);
        Atividade b = atividade("Manhã B", 10, 12, Vagas.ilimitadas(), 0);

        comAlerta.executar(ana, a.getId());
        ResultadoInscricao resultado = comAlerta.executar(ana, b.getId());

        assertTrue(resultado.getInscricao().isAtiva());
        assertEquals(1, resultado.getAvisos().size());
        assertTrue(resultado.getAvisos().get(0).contains("Manhã A"));
        assertEquals(2, inscricoes.listarPorParticipante(ana).size());
    }
}
