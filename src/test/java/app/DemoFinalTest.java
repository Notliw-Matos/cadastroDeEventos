package app;

import application.*;
import domain.model.Atividade;
import domain.model.CriterioFrequencia;
import domain.model.Evento;
import domain.model.Inscricao;
import domain.model.RegistroPresenca;
import domain.model.Usuario;
import domain.model.avaliacao.Pergunta;
import domain.model.avaliacao.Questionario;
import domain.model.avaliacao.RespostaQuestionario;
import domain.model.avaliacao.TipoPergunta;
import domain.strategy.PoliticaConflitoBloqueante;
import domain.vo.Email;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;
import infrastructure.memoria.*;
import infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Fabrica;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S8: testa os cenários CA-01 a CA-07 como testes JUnit automatizados (não só a demo visual).
 * Usa apenas repositórios em memória — nenhuma dependência de banco ou HTTP.
 * Prova que o sistema é testável sem interface (ROO-12) e que todos os cenários passam.
 */
class DemoFinalTest {

    private UsuarioRepositoryEmMemoria usuarios;
    private EventoRepositoryEmMemoria eventos;
    private AtividadeRepositoryEmMemoria atividades;
    private InscricaoRepositoryEmMemoria inscricoes;
    private RegistroFrequenciaRepositoryEmMemoria registros;
    private QuestionarioRepositoryEmMemoria questionarios;
    private RespostaQuestionarioRepositoryEmMemoria respostas;

    private BCryptPasswordHasher hasher;
    private CadastrarUsuarioUseCase cadastrar;
    private AutenticarUsuarioUseCase autenticar;
    private CriarEventoUseCase criarEvento;
    private CriarAtividadeUseCase criarAtividade;
    private RealizarInscricaoUseCase inscrever;
    private VerAgendaUseCase verAgenda;
    private ListarEventosPublicosUseCase listarEventos;
    private RegistrarPresencaUseCase registrarPresenca;
    private CriarQuestionarioUseCase criarQuestionario;
    private ResponderQuestionarioUseCase responderQuestionario;
    private ConsultarConsolidacaoUseCase consultarConsolidacao;
    private GerarRelatorioUseCase gerarRelatorio;

    private UUID orgId;
    private LocalDateTime baseEvento;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryEmMemoria();
        eventos = new EventoRepositoryEmMemoria();
        atividades = new AtividadeRepositoryEmMemoria();
        inscricoes = new InscricaoRepositoryEmMemoria();
        registros = new RegistroFrequenciaRepositoryEmMemoria();
        questionarios = new QuestionarioRepositoryEmMemoria();
        respostas = new RespostaQuestionarioRepositoryEmMemoria();
        hasher = new BCryptPasswordHasher();

        cadastrar = new CadastrarUsuarioUseCase(usuarios, hasher);
        autenticar = new AutenticarUsuarioUseCase(usuarios, hasher);
        criarEvento = new CriarEventoUseCase(eventos, usuarios);
        criarAtividade = new CriarAtividadeUseCase(atividades, eventos, usuarios);
        inscrever = new RealizarInscricaoUseCase(inscricoes, atividades, new PoliticaConflitoBloqueante());
        verAgenda = new VerAgendaUseCase(inscricoes, atividades);
        listarEventos = new ListarEventosPublicosUseCase(eventos);
        registrarPresenca = new RegistrarPresencaUseCase(inscricoes, atividades, eventos, usuarios, registros);
        criarQuestionario = new CriarQuestionarioUseCase(questionarios, atividades, eventos, usuarios);
        responderQuestionario = new ResponderQuestionarioUseCase(respostas, questionarios, inscricoes, registros, registrarPresenca);
        consultarConsolidacao = new ConsultarConsolidacaoUseCase(questionarios, respostas, atividades, eventos, usuarios);
        gerarRelatorio = new GerarRelatorioUseCase(inscricoes, usuarios, atividades, eventos, registros);

        Usuario org = new Usuario(null, "Org", new Email("org@teste.com"), hasher.hash("Senha123"), Usuario.Perfil.ORGANIZADOR);
        usuarios.salvar(org);
        orgId = org.getId();
        baseEvento = LocalDateTime.now().plusDays(20).withHour(8).withMinute(0).withSecond(0).withNano(0);
    }

    // ---- CA-01 Publicação ----

    @Test
    void ca01_eventoNasceEmRascunhoEFicaVisivelAposPublicar() {
        Evento ev = criarEvento.executar(orgId, "Evento Teste", null,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(8)), "Auditório");

        assertEquals(Evento.Estado.RASCUNHO, ev.getEstado());
        assertTrue(listarEventos.executar().isEmpty(), "rascunho nao aparece para visitante");

        ev.publicar();
        eventos.salvar(ev);

        assertEquals(1, listarEventos.executar().size());
        assertEquals("Evento Teste", listarEventos.executar().get(0).paraMapa().get("titulo"));
    }

    @Test
    void ca01_atividadesAparecem() {
        Evento ev = criarEvento.executar(orgId, "Ev", null,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(8)), "x");
        ev.publicar(); eventos.salvar(ev);
        criarAtividade.executar(orgId, ev.getId(), "Palestra", Atividade.TipoAtividade.PALESTRA,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(1)), Vagas.limitadas(50));
        criarAtividade.executar(orgId, ev.getId(), "Oficina", Atividade.TipoAtividade.OFICINA,
                new IntervaloTempo(baseEvento.plusHours(2), baseEvento.plusHours(4)), Vagas.ilimitadas());

        assertEquals(2, new ListarAtividadesDoEventoUseCase(atividades, eventos).executar(ev.getId()).size());
    }

    @Test
    void ca01_participanteNaoCriaEvento() {
        UsuarioResumo part = cadastrar.executar("P", "p@t.com", "Senha123");
        assertThrows(domain.exception.AcessoNegadoException.class,
                () -> criarEvento.executar(part.getId(), "Proibido", null,
                        new IntervaloTempo(baseEvento, baseEvento.plusHours(1)), "x"));
    }

    // ---- CA-02 Inscrição ----

    @Test
    void ca02_inscricaoCompletaComControleDeDuplicidadeEVagas() {
        Evento ev = criarEvento.executar(orgId, "Ev", null,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(4)), "x");
        ev.publicar(); eventos.salvar(ev);
        Atividade atv = criarAtividade.executar(orgId, ev.getId(), "Palestra", Atividade.TipoAtividade.PALESTRA,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(1)), Vagas.limitadas(1));

        UsuarioResumo p1 = cadastrar.executar("Ana", "ana@t.com", "Senha123");
        inscrever.executar(p1.getId(), atv.getId());

        // e-mail duplicado
        assertThrows(domain.exception.EmailJaCadastradoException.class,
                () -> cadastrar.executar("Outra", "ana@t.com", "Senha123"));

        // sem vaga
        UsuarioResumo p2 = cadastrar.executar("Bruno", "bruno@t.com", "Senha123");
        assertThrows(domain.exception.VagasEsgotadasException.class,
                () -> inscrever.executar(p2.getId(), atv.getId()));

        // duplicidade de inscrição
        assertThrows(domain.exception.InscricaoDuplicadaException.class,
                () -> inscrever.executar(p1.getId(), atv.getId()));
    }

    // ---- CA-03 Agenda ----

    @Test
    void ca03_agendaCronologicaEConflitoDeHorarioBloqueado() {
        Evento ev = criarEvento.executar(orgId, "Ev", null,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(8)), "x");
        ev.publicar(); eventos.salvar(ev);
        Atividade a1 = criarAtividade.executar(orgId, ev.getId(), "A1", Atividade.TipoAtividade.PALESTRA,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(1)), Vagas.ilimitadas());
        Atividade a2 = criarAtividade.executar(orgId, ev.getId(), "A2", Atividade.TipoAtividade.OFICINA,
                new IntervaloTempo(baseEvento.plusHours(2), baseEvento.plusHours(4)), Vagas.ilimitadas());
        Atividade conflitante = criarAtividade.executar(orgId, ev.getId(), "Conflito", Atividade.TipoAtividade.PALESTRA,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(1)), Vagas.ilimitadas());

        UsuarioResumo p = cadastrar.executar("Ana", "ana@t.com", "Senha123");
        inscrever.executar(p.getId(), a1.getId());
        inscrever.executar(p.getId(), a2.getId());

        List<ItemAgendaDTO> agenda = verAgenda.executar(p.getId());
        assertEquals(2, agenda.size());
        assertTrue(agenda.get(0).getInicio().compareTo(agenda.get(1).getInicio()) <= 0, "deve estar em ordem cronologica");

        assertThrows(domain.exception.ConflitoDeHorarioException.class,
                () -> inscrever.executar(p.getId(), conflitante.getId()));
    }

    // ---- CA-04 Frequência QR ----

    @Test
    void ca04_criterioCheckInUnicoEEntradaSaida() {
        Evento ev = criarEvento.executar(orgId, "Ev", null,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(8)), "x");
        ev.publicar(); eventos.salvar(ev);

        Atividade aCheckIn = criarAtividade.executar(orgId, ev.getId(), "A1", Atividade.TipoAtividade.PALESTRA,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(1)), Vagas.ilimitadas());
        Atividade aEntradaSaida = new Atividade(null, ev.getId(), "A2", Atividade.TipoAtividade.OFICINA,
                null, null, new IntervaloTempo(baseEvento.plusHours(2), baseEvento.plusHours(4)),
                Vagas.ilimitadas(), CriterioFrequencia.ENTRADA_SAIDA, 0);
        atividades.salvar(aEntradaSaida);

        UsuarioResumo p = cadastrar.executar("Ana", "ana@t.com", "Senha123");
        Inscricao i1 = inscrever.executar(p.getId(), aCheckIn.getId()).getInscricao();
        Inscricao i2 = inscrever.executar(p.getId(), aEntradaSaida.getId()).getInscricao();

        // CHECK_IN_UNICO: basta um check-in
        registrarPresenca.registrarMarcacao(i1.getId(), RegistroPresenca.TipoMarcacao.CHECK_IN, orgId);
        assertTrue(registrarPresenca.calcularSituacaoDePresenca(i1.getId()));

        // ENTRADA_SAIDA: só entrada não basta
        registrarPresenca.registrarMarcacao(i2.getId(), RegistroPresenca.TipoMarcacao.CHECK_IN, orgId);
        assertFalse(registrarPresenca.calcularSituacaoDePresenca(i2.getId()));
        registrarPresenca.registrarMarcacao(i2.getId(), RegistroPresenca.TipoMarcacao.CHECK_OUT, orgId);
        assertTrue(registrarPresenca.calcularSituacaoDePresenca(i2.getId()));
    }

    // ---- CA-05 Manual ----

    @Test
    void ca05_lancamentoManualComRastreabilidadeEBloqueioPorRN18() {
        Evento ev = criarEvento.executar(orgId, "Ev", null,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(4)), "x");
        ev.publicar(); eventos.salvar(ev);
        Atividade atv = criarAtividade.executar(orgId, ev.getId(), "A", Atividade.TipoAtividade.PALESTRA,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(1)), Vagas.ilimitadas());

        UsuarioResumo p = cadastrar.executar("Ana", "ana@t.com", "Senha123");
        Inscricao insc = inscrever.executar(p.getId(), atv.getId()).getInscricao();

        registrarPresenca.registrarMarcacao(insc.getId(), RegistroPresenca.TipoMarcacao.MANUAL, orgId);
        List<RegistroPresenca> regs = registros.listarPorInscricao(insc.getId());
        assertEquals(1, regs.size());
        assertEquals(orgId, regs.get(0).getOperadorId(), "operador deve ser rastreado — RN-11");
        assertTrue(registrarPresenca.calcularSituacaoDePresenca(insc.getId()));

        // RN-18: outro organizador não mexe
        Usuario outroOrg = new Usuario(null, "Outro", new Email("outro@t.com"), "h", Usuario.Perfil.ORGANIZADOR);
        usuarios.salvar(outroOrg);
        assertThrows(domain.exception.AcessoNegadoException.class,
                () -> registrarPresenca.registrarMarcacao(insc.getId(), RegistroPresenca.TipoMarcacao.MANUAL, outroOrg.getId()));
    }

    // ---- CA-06 Avaliação ----

    @Test
    void ca06_avaliacaoCompletoDeElegibilidadeConsolidacaoEUnicidade() {
        Evento ev = criarEvento.executar(orgId, "Ev", null,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(4)), "x");
        ev.publicar(); eventos.salvar(ev);
        Atividade atv = criarAtividade.executar(orgId, ev.getId(), "Palestra", Atividade.TipoAtividade.PALESTRA,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(1)), Vagas.ilimitadas());

        UsuarioResumo p1 = cadastrar.executar("Ana", "ana@t.com", "Senha123");
        UsuarioResumo semPresenca = cadastrar.executar("Bob", "bob@t.com", "Senha123");
        Inscricao i1 = inscrever.executar(p1.getId(), atv.getId()).getInscricao();
        inscricoes.salvar(new Inscricao(null, semPresenca.getId(), atv.getId()));

        registrarPresenca.registrarMarcacao(i1.getId(), RegistroPresenca.TipoMarcacao.CHECK_IN, orgId);

        Pergunta pEscala = new Pergunta(null, "Nota", TipoPergunta.ESCALA_NUMERICA, null);
        Pergunta pTexto = new Pergunta(null, "Comentario", TipoPergunta.TEXTO_LIVRE, null);
        Pergunta pEscolha = new Pergunta(null, "Recomenda?", TipoPergunta.ESCOLHA_UNICA, Arrays.asList("Sim","Nao"));
        Questionario q = criarQuestionario.executar(orgId, atv.getId(), "Avaliacao", List.of(pEscala, pTexto, pEscolha));

        // Ana responde — válido
        RespostaQuestionario r = responderQuestionario.executar(p1.getId(), q.getId(),
                Map.of(pEscala.getId(), "8", pTexto.getId(), "Otimo!", pEscolha.getId(), "Sim"));
        assertEquals(3, r.getRespostas().size());

        // Ana tenta de novo — RN-14
        assertThrows(domain.exception.EstadoInvalidoException.class,
                () -> responderQuestionario.executar(p1.getId(), q.getId(),
                        Map.of(pEscala.getId(), "5", pTexto.getId(), "Mudei", pEscolha.getId(), "Nao")));

        // Bob sem presença — RN-13
        assertThrows(domain.exception.EstadoInvalidoException.class,
                () -> responderQuestionario.executar(semPresenca.getId(), q.getId(),
                        Map.of(pEscala.getId(), "6", pTexto.getId(), "x", pEscolha.getId(), "Sim")));

        ConsolidacaoAvaliacaoDTO consolid = consultarConsolidacao.executar(orgId, q.getId());
        assertEquals(1, (int)(Integer) consolid.paraMapa().get("totalRespostas"));
    }

    // ---- CA-07 Relatório ----

    @Test
    void ca07_relatorioCsvContemDadosCorretosEBloqueiaParticipante() {
        Evento ev = criarEvento.executar(orgId, "Simpósio", null,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(4)), "Auditório");
        ev.publicar(); eventos.salvar(ev);
        Atividade atv = criarAtividade.executar(orgId, ev.getId(), "Palestra", Atividade.TipoAtividade.PALESTRA,
                new IntervaloTempo(baseEvento, baseEvento.plusHours(1)), Vagas.ilimitadas());

        UsuarioResumo ana = cadastrar.executar("Ana Demo", "ana@t.com", "Senha123");
        Inscricao insc = inscrever.executar(ana.getId(), atv.getId()).getInscricao();
        registrarPresenca.registrarMarcacao(insc.getId(), RegistroPresenca.TipoMarcacao.CHECK_IN, orgId);

        // relatório de inscrições
        var relInsc = gerarRelatorio.executar(orgId, atv.getId(), GerarRelatorioUseCase.TipoRelatorio.INSCRICOES);
        String csvInsc = relInsc.toCsv();
        assertTrue(csvInsc.contains("ana@t.com"), "e-mail da Ana deve aparecer");
        assertTrue(csvInsc.contains("CONFIRMADA"), "situacao deve aparecer");
        assertEquals("relatorio_inscricoes.csv", relInsc.nomeArquivo());

        // relatório de frequência
        var relFreq = gerarRelatorio.executar(orgId, atv.getId(), GerarRelatorioUseCase.TipoRelatorio.FREQUENCIA);
        assertTrue(relFreq.toCsv().contains("SIM"), "Ana deve aparecer como PRESENTE");

        // participante não pode gerar
        assertThrows(domain.exception.AcessoNegadoException.class,
                () -> gerarRelatorio.executar(ana.getId(), atv.getId(), GerarRelatorioUseCase.TipoRelatorio.INSCRICOES));
    }
}
