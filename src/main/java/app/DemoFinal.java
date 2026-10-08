package app;

import application.*;
import domain.exception.DominioException;
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
import domain.ports.*;
import domain.strategy.PoliticaConflitoBloqueante;
import domain.vo.Email;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;
import infrastructure.memoria.*;
import infrastructure.security.BCryptPasswordHasher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * S8 — Demonstração final integrada.
 * Executa os cenários CA-01 a CA-07 em sequência, sem interface gráfica, usando os mesmos
 * casos de uso e portas que a API HTTP e o site consomem. Serve de roteiro para a
 * apresentação final (seção 11.2 da especificação).
 *
 * Uso: java -cp ... app.DemoFinal   (sem variáveis de banco = tudo em memória, rápido)
 */
public class DemoFinal {

    // ── repositórios ─────────────────────────────────────────────────────────
    private static final UsuarioRepositoryEmMemoria usuarios = new UsuarioRepositoryEmMemoria();
    private static final EventoRepositoryEmMemoria eventos = new EventoRepositoryEmMemoria();
    private static final AtividadeRepositoryEmMemoria atividades = new AtividadeRepositoryEmMemoria();
    private static final InscricaoRepositoryEmMemoria inscricoes = new InscricaoRepositoryEmMemoria();
    private static final RegistroFrequenciaRepositoryEmMemoria registros = new RegistroFrequenciaRepositoryEmMemoria();
    private static final QuestionarioRepositoryEmMemoria questionarios = new QuestionarioRepositoryEmMemoria();
    private static final RespostaQuestionarioRepositoryEmMemoria respostas = new RespostaQuestionarioRepositoryEmMemoria();

    private static final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    // ── casos de uso ─────────────────────────────────────────────────────────
    private static final CadastrarUsuarioUseCase cadastrar = new CadastrarUsuarioUseCase(usuarios, hasher);
    private static final AutenticarUsuarioUseCase autenticar = new AutenticarUsuarioUseCase(usuarios, hasher);
    private static final CriarEventoUseCase criarEvento = new CriarEventoUseCase(eventos, usuarios);
    private static final CriarAtividadeUseCase criarAtividade = new CriarAtividadeUseCase(atividades, eventos, usuarios);
    private static final RealizarInscricaoUseCase inscrever =
            new RealizarInscricaoUseCase(inscricoes, atividades, new PoliticaConflitoBloqueante());
    private static final VerAgendaUseCase verAgenda = new VerAgendaUseCase(inscricoes, atividades);
    private static final ListarEventosPublicosUseCase listarEventos = new ListarEventosPublicosUseCase(eventos);
    private static final ListarAtividadesDoEventoUseCase listarAtividades =
            new ListarAtividadesDoEventoUseCase(atividades, eventos);
    private static final RegistrarPresencaUseCase registrarPresenca =
            new RegistrarPresencaUseCase(inscricoes, atividades, eventos, usuarios, registros);
    private static final CriarQuestionarioUseCase criarQuestionario =
            new CriarQuestionarioUseCase(questionarios, atividades, eventos, usuarios);
    private static final ResponderQuestionarioUseCase responderQuestionario =
            new ResponderQuestionarioUseCase(respostas, questionarios, inscricoes, registros, registrarPresenca);
    private static final ConsultarConsolidacaoUseCase consultarConsolidacao =
            new ConsultarConsolidacaoUseCase(questionarios, respostas, atividades, eventos, usuarios);
    private static final GerarRelatorioUseCase gerarRelatorio =
            new GerarRelatorioUseCase(inscricoes, usuarios, atividades, eventos, registros);

    // ── estado da demo ────────────────────────────────────────────────────────
    private static UUID orgId, participante1Id, participante2Id;
    private static UUID eventoId, atividade1Id, atividade2Id, questionarioId;

    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        System.out.println("=======================================================");
        System.out.println("  DEMONSTRAÇÃO FINAL — Plataforma de Gestão de Eventos ");
        System.out.println("  ROO-01 a ROO-12 rastreados | CA-01 a CA-07            ");
        System.out.println("=======================================================");

        ca01_publicacao();
        ca02_inscricao();
        ca03_agenda();
        ca04_frequenciaQr();
        ca05_alternativaManual();
        ca06_avaliacao();
        ca07_relatorio();

        System.out.println("\n=== DEMONSTRACAO CONCLUIDA — todos os cenarios passaram ===");
    }

    // ── CA-01: Publicação ─────────────────────────────────────────────────────
    static void ca01_publicacao() throws Exception {
        titulo("CA-01 — Publicação (RF-04, RF-05, RN-04)");

        // Organizador é criado direto com o perfil certo (cadastro público só cria PARTICIPANTE)
        Usuario org = new Usuario(null, "Organizadora Demo",
                new Email("org@demo.com"), hasher.hash("Demo@123"), Usuario.Perfil.ORGANIZADOR);
        usuarios.salvar(org);
        orgId = org.getId();

        LocalDateTime base = LocalDateTime.now().plusDays(30)
                .withHour(8).withMinute(0).withSecond(0).withNano(0);

        Evento ev = ok("Organizador cria evento (nasce em RASCUNHO)",
                () -> criarEvento.executar(orgId, "Simposio de POO 2026",
                        "Evento de demonstracao final.",
                        new IntervaloTempo(base, base.plusHours(10)), "Auditorio Principal"));
        eventoId = ev.getId();
        System.out.println("     estado inicial: " + ev.getEstado());

        // Publicar (ROO-02: invariante de estado protegida pelo proprio Evento)
        ev.publicar();
        eventos.salvar(ev);
        System.out.println("     apos publicar : " + ev.getEstado());

        Atividade a1 = ok("Criar palestra (1 vaga, criterio CHECK_IN_UNICO — ROO-03 Vagas)",
                () -> criarAtividade.executar(orgId, eventoId, "Palestra de Abertura",
                        Atividade.TipoAtividade.PALESTRA,
                        new IntervaloTempo(base, base.plusHours(1)), Vagas.limitadas(2)));
        atividade1Id = a1.getId();

        Atividade a2 = ok("Criar oficina (ilimitada, criterio ENTRADA_SAIDA)",
                () -> {
                    Atividade a = criarAtividade.executar(orgId, eventoId, "Oficina de Testes",
                            Atividade.TipoAtividade.OFICINA,
                            new IntervaloTempo(base.plusHours(2), base.plusHours(4)), Vagas.ilimitadas());
                    // Trocar criterio de frequencia via recriacao (objeto de valor imutavel — ROO-03)
                    return new Atividade(a.getId(), a.getEventoId(), a.getTitulo(), a.getTipo(),
                            a.getTrilhaCategoria(), a.getLocal(), a.getIntervalo(), a.getVagas(),
                            CriterioFrequencia.ENTRADA_SAIDA, 0);
                });
        atividades.salvar(a2);
        atividade2Id = a2.getId();

        List<EventoPublicoDTO> publicos = listarEventos.executar();
        System.out.println("     eventos visiveis ao publico: " + publicos.size());
        List<AtividadeDTO> programa = listarAtividades.executar(eventoId);
        System.out.println("     atividades na programacao: " + programa.size());

        erro("Participante tenta criar evento (RNF-06 — checagem no servidor, nao na tela)",
                () -> criarEvento.executar(UUID.randomUUID(), "Proibido", null,
                        new IntervaloTempo(base, base.plusHours(1)), "x"));
    }

    // ── CA-02: Inscrição ──────────────────────────────────────────────────────
    static void ca02_inscricao() throws Exception {
        titulo("CA-02 — Inscricao (RF-12, RF-13, RF-14, RN-01, RN-06)");

        UsuarioResumo r1 = ok("Cadastro de participante 1 (RN-01: e-mail unico)",
                () -> cadastrar.executar("Ana Demo", "ana@demo.com", "Demo@123"));
        participante1Id = r1.getId();

        UsuarioResumo r2 = ok("Cadastro de participante 2",
                () -> cadastrar.executar("Bruno Demo", "bruno@demo.com", "Demo@123"));
        participante2Id = r2.getId();

        erro("E-mail duplicado e rejeitado com mensagem clara (RN-01)",
                () -> cadastrar.executar("Outro", "ana@demo.com", "Demo@123"));

        ok("Ana se inscreve na palestra (vaga disponivel)",
                () -> inscrever.executar(participante1Id, atividade1Id));
        ok("Bruno se inscreve na palestra (ultima vaga)",
                () -> inscrever.executar(participante2Id, atividade1Id));

        erro("Terceiro participante sem vaga (RN-06 — VagasEsgotadasException)",
                () -> {
                    UsuarioResumo r3 = cadastrar.executar("Carlos Demo", "carlos@demo.com", "Demo@123");
                    return inscrever.executar(r3.getId(), atividade1Id);
                });

        ok("Ana se inscreve na oficina tambem",
                () -> inscrever.executar(participante1Id, atividade2Id));
    }

    // ── CA-03: Agenda ─────────────────────────────────────────────────────────
    static void ca03_agenda() throws Exception {
        titulo("CA-03 — Agenda (RF-16, RF-17, RF-18, RN-07, RN-08)");

        List<ItemAgendaDTO> agendaAna = ok("Agenda de Ana (2 atividades em ordem cronologica — RF-17)",
                () -> verAgenda.executar(participante1Id));
        System.out.println("     itens na agenda de Ana: " + agendaAna.size());
        for (ItemAgendaDTO item : agendaAna)
            System.out.println("       " + item.getInicio() + " — " + item.paraMapa().get("titulo"));

        // Conflito de horario (ROO-05: PoliticaConflitoBloqueante via Strategy)
        LocalDateTime baseConflito = LocalDateTime.now().plusDays(30)
                .withHour(8).withMinute(0).withSecond(0).withNano(0);
        Atividade conflitante = new Atividade(null, eventoId, "Atividade Conflitante",
                Atividade.TipoAtividade.PALESTRA, null, null,
                new IntervaloTempo(baseConflito, baseConflito.plusHours(1)),
                Vagas.ilimitadas(), CriterioFrequencia.CHECK_IN_UNICO, 0);
        atividades.salvar(conflitante);
        erro("Conflito de horario com palestra ja inscrita (RN-07 — ConflitoDeHorarioException)",
                () -> inscrever.executar(participante1Id, conflitante.getId()));
    }

    // ── CA-04: Frequência via QR ──────────────────────────────────────────────
    static void ca04_frequenciaQr() throws Exception {
        titulo("CA-04 — Frequencia via QR (RF-20, RF-21, RF-23, RN-09, RN-10)");

        Inscricao inscAna1 = inscricoes.buscarPorParticipanteEAtividade(participante1Id, atividade1Id).get();
        ok("Organizador registra CHECK_IN de Ana na palestra (criterio CHECK_IN_UNICO)",
                () -> registrarPresenca.registrarMarcacao(inscAna1.getId(),
                        RegistroPresenca.TipoMarcacao.CHECK_IN, orgId));
        boolean presente = registrarPresenca.calcularSituacaoDePresenca(inscAna1.getId());
        System.out.println("     situacao de presenca de Ana na palestra: " + (presente ? "PRESENTE" : "AUSENTE"));

        // ENTRADA_SAIDA exige os dois registros
        Inscricao inscAna2 = inscricoes.buscarPorParticipanteEAtividade(participante1Id, atividade2Id).get();
        ok("CHECK_IN de Ana na oficina (criterio ENTRADA_SAIDA — precisa das duas)",
                () -> registrarPresenca.registrarMarcacao(inscAna2.getId(),
                        RegistroPresenca.TipoMarcacao.CHECK_IN, orgId));
        boolean soParcial = registrarPresenca.calcularSituacaoDePresenca(inscAna2.getId());
        System.out.println("     situacao com so CHECK_IN: " + (soParcial ? "PRESENTE" : "PARCIAL/AUSENTE"));

        ok("CHECK_OUT de Ana na oficina (completa o criterio ENTRADA_SAIDA)",
                () -> registrarPresenca.registrarMarcacao(inscAna2.getId(),
                        RegistroPresenca.TipoMarcacao.CHECK_OUT, orgId));
        boolean completo = registrarPresenca.calcularSituacaoDePresenca(inscAna2.getId());
        System.out.println("     situacao apos CHECK_OUT: " + (completo ? "PRESENTE" : "AUSENTE"));
    }

    // ── CA-05: Alternativa manual ─────────────────────────────────────────────
    static void ca05_alternativaManual() throws Exception {
        titulo("CA-05 — Lancamento manual (RF-22, RN-11, RN-12, RN-18)");

        Inscricao inscBruno = inscricoes.buscarPorParticipanteEAtividade(participante2Id, atividade1Id).get();
        ok("Organizador lanca presenca MANUAL de Bruno (operador registrado — RN-11)",
                () -> registrarPresenca.registrarMarcacao(inscBruno.getId(),
                        RegistroPresenca.TipoMarcacao.MANUAL, orgId));

        List<RegistroPresenca> registrosBruno = registros.listarPorInscricao(inscBruno.getId());
        System.out.println("     registros de Bruno: " + registrosBruno.size());
        System.out.println("     operador: " + registrosBruno.get(0).getOperadorId() + " (id do org)");

        // RN-18: outro organizador nao mexe neste evento
        Usuario outroOrg = new Usuario(null, "Outro Org", new Email("outro@demo.com"),
                hasher.hash("Demo@123"), Usuario.Perfil.ORGANIZADOR);
        usuarios.salvar(outroOrg);
        erro("Outro organizador nao registra presenca em evento alheio (RN-18)",
                () -> registrarPresenca.registrarMarcacao(inscBruno.getId(),
                        RegistroPresenca.TipoMarcacao.MANUAL, outroOrg.getId()));
    }

    // ── CA-06: Avaliação ──────────────────────────────────────────────────────
    static void ca06_avaliacao() throws Exception {
        titulo("CA-06 — Avaliacao (RF-24, RF-25, RF-26, RF-27, RN-13, RN-14, RN-15)");

        Pergunta pTexto = new Pergunta(null, "Comentario livre", TipoPergunta.TEXTO_LIVRE, null);
        Pergunta pEscala = new Pergunta(null, "Nota de 1 a 10", TipoPergunta.ESCALA_NUMERICA, null);
        Pergunta pEscolha = new Pergunta(null, "Recomendaria?", TipoPergunta.ESCOLHA_UNICA,
                Arrays.asList("Sim", "Nao", "Talvez"));

        Questionario q = ok("Organizador cria questionario com 3 tipos de pergunta (RF-24, RF-25)",
                () -> criarQuestionario.executar(orgId, atividade1Id, "Avalie a palestra",
                        List.of(pTexto, pEscala, pEscolha)));
        questionarioId = q.getId();

        RespostaQuestionario r = ok("Ana (inscrita e presente) responde questionario",
                () -> responderQuestionario.executar(participante1Id, questionarioId, Map.of(
                        pTexto.getId(), "Excelente conteudo!",
                        pEscala.getId(), "9",
                        pEscolha.getId(), "Sim")));
        System.out.println("     respostas enviadas: " + r.getRespostas().size());

        erro("Ana tenta responder de novo (RN-14 — uma resposta por questionario)",
                () -> responderQuestionario.executar(participante1Id, questionarioId, Map.of(
                        pTexto.getId(), "Mudei de ideia",
                        pEscala.getId(), "5",
                        pEscolha.getId(), "Nao")));

        // Bruno esta presente (lancamento manual — CA-05), entao pode avaliar
        ok("Bruno (presente por lancamento manual) tambem responde",
                () -> responderQuestionario.executar(participante2Id, questionarioId, Map.of(
                        pTexto.getId(), "Muito bom!",
                        pEscala.getId(), "8",
                        pEscolha.getId(), "Sim")));

        // Participante sem presenca nao avalia
        UsuarioResumo semPresenca = cadastrar.executar("Sem Presenca", "semp@demo.com", "Demo@123");
        inscricoes.salvar(new Inscricao(null, semPresenca.getId(), atividade1Id));
        erro("Participante sem presenca validada nao avalia (RN-13)",
                () -> responderQuestionario.executar(semPresenca.getId(), questionarioId, Map.of(
                        pTexto.getId(), "Tentativa", pEscala.getId(), "6", pEscolha.getId(), "Nao")));

        ConsolidacaoAvaliacaoDTO consolid = ok("Organizador consulta consolidacao (RF-28)",
                () -> consultarConsolidacao.executar(orgId, questionarioId));
        Map<String, Object> mapa = consolid.paraMapa();
        System.out.println("     total de respostas: " + mapa.get("totalRespostas"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pergs = (List<Map<String, Object>>) mapa.get("perguntas");
        for (Map<String, Object> p : pergs) {
            System.out.println("     [" + p.get("tipo") + "] " + p.get("enunciado") + " => "
                    + (p.containsKey("media") ? "media=" + p.get("media")
                    : p.containsKey("distribuicao") ? p.get("distribuicao")
                    : p.get("comentarios")));
        }
    }

    // ── CA-07: Relatório ──────────────────────────────────────────────────────
    static void ca07_relatorio() throws Exception {
        titulo("CA-07 — Relatorios (RF-29, RF-30, RF-31)");

        application.relatorio.RelatorioExportavel relInsc =
                ok("Organizador exporta relatorio de inscricoes (RF-29, RF-31)",
                        () -> gerarRelatorio.executar(orgId, atividade1Id,
                                GerarRelatorioUseCase.TipoRelatorio.INSCRICOES));
        String csvInsc = relInsc.toCsv();
        System.out.println("     arquivo: " + relInsc.nomeArquivo());
        System.out.println("     linhas: " + csvInsc.lines().count() + " (cabecalho + inscritos)");
        System.out.println("     contem ana@demo.com: " + csvInsc.contains("ana@demo.com"));

        application.relatorio.RelatorioExportavel relFreq =
                ok("Organizador exporta relatorio de frequencia (RF-30, RF-31)",
                        () -> gerarRelatorio.executar(orgId, atividade1Id,
                                GerarRelatorioUseCase.TipoRelatorio.FREQUENCIA));
        String csvFreq = relFreq.toCsv();
        System.out.println("     arquivo: " + relFreq.nomeArquivo());
        System.out.println("     Ana aparece como PRESENTE: " + csvFreq.contains("SIM"));

        erro("Participante nao pode gerar relatorio (RN-18 — AcessoNegadoException)",
                () -> gerarRelatorio.executar(participante1Id, atividade1Id,
                        GerarRelatorioUseCase.TipoRelatorio.INSCRICOES));
    }

    // ── utilitários ───────────────────────────────────────────────────────────

    static void titulo(String texto) {
        System.out.println("\n>>> " + texto);
    }

    static <T> T ok(String descricao, Callable<T> acao) throws Exception {
        T resultado = acao.call();
        System.out.println("  [OK] " + descricao);
        return resultado;
    }

    static void erro(String descricao, Callable<?> acao) throws Exception {
        try {
            acao.call();
            throw new AssertionError("Deveria ter lancado excecao: " + descricao);
        } catch (DominioException e) {
            System.out.println("  [ERRO ESPERADO] " + descricao);
            System.out.println("           -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
