package app;

import com.sun.net.httpserver.HttpServer;
import domain.model.Atividade;
import domain.model.Evento;
import domain.model.Usuario;
import domain.vo.Email;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;
import infrastructure.http.Json;
import infrastructure.http.TokenStore;
import infrastructure.memoria.AtividadeRepositoryEmMemoria;
import infrastructure.memoria.EventoRepositoryEmMemoria;
import infrastructure.memoria.InscricaoRepositoryEmMemoria;
import infrastructure.memoria.UsuarioRepositoryEmMemoria;
import infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de INTEGRAÇÃO da API HTTP (S4): sobem o servidor de verdade (HttpServer real, numa
 * porta livre) dentro da própria JVM de teste e conversam com ele por HTTP de verdade
 * (java.net.http.HttpClient), sem mocks na borda. Evita depender de curl/shell (instável neste
 * ambiente) e fica como evidência automatizada permanente do comportamento da API.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServidorApiTest {

    private HttpServer servidor;
    private String base;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    private UsuarioRepositoryEmMemoria usuarios;
    private EventoRepositoryEmMemoria eventos;
    private AtividadeRepositoryEmMemoria atividades;
    private InscricaoRepositoryEmMemoria inscricoes;
    private UUID organizadorId;

    @BeforeAll
    void subirServidor() throws Exception {
        usuarios = new UsuarioRepositoryEmMemoria();
        eventos = new EventoRepositoryEmMemoria();
        atividades = new AtividadeRepositoryEmMemoria();
        inscricoes = new InscricaoRepositoryEmMemoria();
        var registrosFrequencia = new infrastructure.memoria.RegistroFrequenciaRepositoryEmMemoria();
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();

        // organizador de teste: inserido direto no repositório (não existe endpoint público para
        // criar organizador — cadastro público sempre gera PARTICIPANTE, de propósito)
        Usuario organizador = new Usuario(null, "Organizadora Teste", new Email("organizadora@teste.com"),
                hasher.hash("Senha123"), Usuario.Perfil.ORGANIZADOR);
        usuarios.salvar(organizador);
        organizadorId = organizador.getId();

        LocalDateTime inicio = LocalDateTime.now().plusDays(15).withHour(9).withMinute(0).withSecond(0).withNano(0);
        Evento eventoPublicado = Evento.novo(organizador.getId(), "Evento Publicado de Teste", "desc",
                new IntervaloTempo(inicio, inicio.plusHours(8)), "Sala 1");
        eventoPublicado.publicar();
        eventos.salvar(eventoPublicado);
        Evento eventoRascunho = Evento.novo(organizador.getId(), "Evento Rascunho (não deve aparecer)", null,
                new IntervaloTempo(inicio, inicio.plusHours(2)), "Sala 2");
        eventos.salvar(eventoRascunho);

        Atividade atividadeComUmaVaga = Atividade.nova(eventoPublicado.getId(), "Oficina com 1 vaga",
                Atividade.TipoAtividade.OFICINA, new IntervaloTempo(inicio, inicio.plusHours(1)), Vagas.limitadas(1));
        atividades.salvar(atividadeComUmaVaga);

        servidor = ServidorApi.criar(0, usuarios, eventos, atividades, inscricoes, registrosFrequencia,
                hasher, new TokenStore());
        servidor.start();
        base = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterAll
    void pararServidor() {
        servidor.stop(0);
    }

    private HttpResponse<String> get(String caminho, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(base + caminho)).timeout(Duration.ofSeconds(3)).GET();
        if (token != null) req.header("Authorization", "Bearer " + token);
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String caminho, String corpoJson, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(base + caminho)).timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpoJson));
        if (token != null) req.header("Authorization", "Bearer " + token);
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String tokenDe(String email, String senha) throws Exception {
        HttpResponse<String> resp = post("/api/sessoes", "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}", null);
        assertEquals(200, resp.statusCode(), "login deveria funcionar: " + resp.body());
        return (String) Json.decodeObjeto(resp.body()).get("token");
    }

    // ---------------- RF-10: site público ----------------

    @Test
    void eventosPublicadosApareceMasRascunhoNao() throws Exception {
        HttpResponse<String> resp = get("/api/eventos", null);
        assertEquals(200, resp.statusCode());
        String corpo = resp.body();
        assertTrue(corpo.contains("Evento Publicado de Teste"));
        assertFalse(corpo.contains("Evento Rascunho"));
    }

    @Test
    void listaAtividadesDoEventoPublico() throws Exception {
        HttpResponse<String> resp = get("/api/eventos/" + eventoPublicadoId() + "/atividades", null);
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("Oficina com 1 vaga"));
    }

    // ---------------- RF-01, RN-01: cadastro ----------------

    @Test
    void cadastraParticipanteEDepoisRejeitaEmailDuplicado() throws Exception {
        HttpResponse<String> primeiro = post("/api/usuarios",
                "{\"nome\":\"Carla\",\"email\":\"carla.http@teste.com\",\"senha\":\"Senha123\"}", null);
        assertEquals(201, primeiro.statusCode(), primeiro.body());
        assertTrue(primeiro.body().contains("PARTICIPANTE"));

        HttpResponse<String> duplicado = post("/api/usuarios",
                "{\"nome\":\"Outra\",\"email\":\"carla.http@teste.com\",\"senha\":\"Outra1234\"}", null);
        assertEquals(400, duplicado.statusCode());
        assertTrue(duplicado.body().contains("já está cadastrado"));
    }

    // ---------------- RF-02: login ----------------

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        post("/api/usuarios", "{\"nome\":\"Dan\",\"email\":\"dan.http@teste.com\",\"senha\":\"Senha123\"}", null);
        HttpResponse<String> resp = post("/api/sessoes",
                "{\"email\":\"dan.http@teste.com\",\"senha\":\"Errada123\"}", null);
        assertEquals(401, resp.statusCode());
    }

    // ---------------- RNF-06: autorização no servidor ----------------

    @Test
    void criarEventoSemTokenRetorna401() throws Exception {
        HttpResponse<String> resp = post("/api/eventos",
                "{\"titulo\":\"x\",\"inicio\":\"2026-11-01T08:00:00\",\"fim\":\"2026-11-01T10:00:00\"}", null);
        assertEquals(401, resp.statusCode());
    }

    @Test
    void participanteNaoCriaEvento() throws Exception {
        post("/api/usuarios", "{\"nome\":\"Erica\",\"email\":\"erica.http@teste.com\",\"senha\":\"Senha123\"}", null);
        String tokenParticipante = tokenDe("erica.http@teste.com", "Senha123");

        HttpResponse<String> resp = post("/api/eventos",
                "{\"titulo\":\"Proibido\",\"inicio\":\"2026-11-01T08:00:00\",\"fim\":\"2026-11-01T10:00:00\"}",
                tokenParticipante);
        assertEquals(403, resp.statusCode());
    }

    @Test
    void organizadorCriaEventoEAtividade() throws Exception {
        String tokenOrganizador = tokenDe("organizadora@teste.com", "Senha123");

        HttpResponse<String> criarEvento = post("/api/eventos",
                "{\"titulo\":\"Evento via HTTP\",\"descricao\":\"d\",\"inicio\":\"2026-11-05T08:00:00\"," +
                        "\"fim\":\"2026-11-05T18:00:00\",\"local\":\"Auditório\"}", tokenOrganizador);
        assertEquals(201, criarEvento.statusCode(), criarEvento.body());
        String eventoId = (String) Json.decodeObjeto(criarEvento.body()).get("id");

        HttpResponse<String> criarAtividade = post("/api/eventos/" + eventoId + "/atividades",
                "{\"titulo\":\"Palestra\",\"tipo\":\"PALESTRA\",\"inicio\":\"2026-11-05T09:00:00\"," +
                        "\"fim\":\"2026-11-05T10:00:00\"}", tokenOrganizador);
        assertEquals(201, criarAtividade.statusCode(), criarAtividade.body());
        assertTrue((Boolean) Json.decodeObjeto(criarAtividade.body()).get("temVaga"));
    }

    // ---------------- RF-12, RN-06: inscrição e vagas ----------------

    @Test
    void inscricaoRespeitaLimiteDeVagas() throws Exception {
        post("/api/usuarios", "{\"nome\":\"Fabio\",\"email\":\"fabio.http@teste.com\",\"senha\":\"Senha123\"}", null);
        post("/api/usuarios", "{\"nome\":\"Gabi\",\"email\":\"gabi.http@teste.com\",\"senha\":\"Senha123\"}", null);
        String tokenFabio = tokenDe("fabio.http@teste.com", "Senha123");
        String tokenGabi = tokenDe("gabi.http@teste.com", "Senha123");
        String atividadeId = atividadeComUmaVagaId();

        HttpResponse<String> primeira = post("/api/inscricoes", "{\"atividadeId\":\"" + atividadeId + "\"}", tokenFabio);
        assertEquals(201, primeira.statusCode(), primeira.body());

        HttpResponse<String> segunda = post("/api/inscricoes", "{\"atividadeId\":\"" + atividadeId + "\"}", tokenGabi);
        assertEquals(400, segunda.statusCode());
        assertTrue(segunda.body().contains("vaga"));
    }

    // ---------------- protocolo HTTP ----------------

    @Test
    void rotaInexistenteRetorna404() throws Exception {
        assertEquals(404, get("/api/isso-nao-existe", null).statusCode());
    }

    @Test
    void corpoInvalidoRetorna400EmVezDeQuebrarOServidor() throws Exception {
        HttpResponse<String> resp = post("/api/usuarios", "{isto nao e json", null);
        assertEquals(400, resp.statusCode());
    }

    @Test
    void inscricaoTrazListaDeAvisosVaziaQuandoNaoHaConflito() throws Exception {
        post("/api/usuarios", "{\"nome\":\"Helio\",\"email\":\"helio.http@teste.com\",\"senha\":\"Senha123\"}", null);
        String token = tokenDe("helio.http@teste.com", "Senha123");
        String atividadeId = novaAtividadeSemLimiteDeVaga("Palestra avulsa para Helio");
        HttpResponse<String> resp = post("/api/inscricoes",
                "{\"atividadeId\":\"" + atividadeId + "\"}", token);
        assertEquals(201, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("\"avisos\":[]"));
    }

    // ---------------- RF-16, RF-17: agenda pessoal ----------------

    @Test
    void agendaSemLoginRetorna401() throws Exception {
        assertEquals(401, get("/api/agenda", null).statusCode());
    }

    @Test
    void agendaTrazAtividadeInscritaEmOrdemCronologica() throws Exception {
        post("/api/usuarios", "{\"nome\":\"Iris\",\"email\":\"iris.http@teste.com\",\"senha\":\"Senha123\"}", null);
        String token = tokenDe("iris.http@teste.com", "Senha123");

        HttpResponse<String> vazia = get("/api/agenda", token);
        assertEquals(200, vazia.statusCode());
        assertEquals("[]", vazia.body());

        String atividadeId = novaAtividadeSemLimiteDeVaga("Palestra avulsa para Iris");
        post("/api/inscricoes", "{\"atividadeId\":\"" + atividadeId + "\"}", token);
        HttpResponse<String> comItem = get("/api/agenda", token);
        assertEquals(200, comItem.statusCode());
        assertTrue(comItem.body().contains("Palestra avulsa para Iris"));
    }

    // ---------------- auxiliares ----------------

    // ---------------- RF-20 a RF-23: frequência via QR e manual ----------------

    @Test
    void meuCodigoExigeLogin() throws Exception {
        assertEquals(401, get("/api/frequencia/meu-codigo", null).statusCode());
    }

    @Test
    void checkinComCodigoValidoRegistraPresencaEAtualizaSituacao() throws Exception {
        // participante se cadastra, loga e se inscreve numa atividade nova (sem disputa de vaga)
        post("/api/usuarios", "{\"nome\":\"Joao\",\"email\":\"joao.http@teste.com\",\"senha\":\"Senha123\"}", null);
        String[] joao = loginComId("joao.http@teste.com", "Senha123");
        String atividadeId = novaAtividadeSemLimiteDeVaga("Atividade para check-in de Joao");
        post("/api/inscricoes", "{\"atividadeId\":\"" + atividadeId + "\"}", joao[1]);

        // participante gera o código (conteúdo do QR)
        HttpResponse<String> codigoResp = get("/api/frequencia/meu-codigo", joao[1]);
        assertEquals(200, codigoResp.statusCode());
        String codigo = (String) Json.decodeObjeto(codigoResp.body()).get("codigo");
        assertNotNull(codigo);

        // organizador "lê" o código e registra check-in
        String tokenOrganizador = tokenDe("organizadora@teste.com", "Senha123");
        HttpResponse<String> checkin = post("/api/frequencia/checkin",
                "{\"codigo\":\"" + codigo + "\",\"atividadeId\":\"" + atividadeId + "\",\"tipo\":\"CHECK_IN\"}",
                tokenOrganizador);
        assertEquals(201, checkin.statusCode(), checkin.body());

        // situação calculada: atividade nova usa CHECK_IN_UNICO por padrão, então já basta
        String inscricaoId = buscarIdInscricao(inscricoes, joao[0], atividadeId);
        HttpResponse<String> situacao = get("/api/frequencia/" + inscricaoId + "/situacao", tokenOrganizador);
        assertEquals(200, situacao.statusCode());
        assertEquals(Boolean.TRUE, Json.decodeObjeto(situacao.body()).get("presente"));
    }

    @Test
    void checkinComCodigoInvalidoFalha() throws Exception {
        String tokenOrganizador = tokenDe("organizadora@teste.com", "Senha123");
        HttpResponse<String> resp = post("/api/frequencia/checkin",
                "{\"codigo\":\"codigo-que-nao-existe\",\"atividadeId\":\"" + UUID.randomUUID() + "\",\"tipo\":\"CHECK_IN\"}",
                tokenOrganizador);
        assertEquals(400, resp.statusCode());
    }

    @Test
    void lancamentoManualRegistraPresencaSemQrCode() throws Exception {
        post("/api/usuarios", "{\"nome\":\"Lucas\",\"email\":\"lucas.http@teste.com\",\"senha\":\"Senha123\"}", null);
        String[] lucas = loginComId("lucas.http@teste.com", "Senha123");
        String atividadeId = novaAtividadeSemLimiteDeVaga("Atividade para lançamento manual");
        post("/api/inscricoes", "{\"atividadeId\":\"" + atividadeId + "\"}", lucas[1]);

        String tokenOrganizador = tokenDe("organizadora@teste.com", "Senha123");
        HttpResponse<String> resp = post("/api/frequencia/manual",
                "{\"participanteId\":\"" + lucas[0] + "\",\"atividadeId\":\"" + atividadeId + "\"}", tokenOrganizador);
        assertEquals(201, resp.statusCode(), resp.body());
        assertTrue(resp.body().contains("\"tipo\":\"MANUAL\""));
    }

    @Test
    void lancamentoManualSemAutorizacaoFalha() throws Exception {
        post("/api/usuarios", "{\"nome\":\"Mara\",\"email\":\"mara.http@teste.com\",\"senha\":\"Senha123\"}", null);
        String[] mara = loginComId("mara.http@teste.com", "Senha123");
        String atividadeId = novaAtividadeSemLimiteDeVaga("Atividade sem permissão");
        post("/api/inscricoes", "{\"atividadeId\":\"" + atividadeId + "\"}", mara[1]);

        HttpResponse<String> resp = post("/api/frequencia/manual",
                "{\"participanteId\":\"" + mara[0] + "\",\"atividadeId\":\"" + atividadeId + "\"}",
                mara[1]); // participante tentando registrar a própria presença sozinho
        assertEquals(403, resp.statusCode());
    }

    // ---------------- RF-24 a RF-28: avaliações ----------------

    @Test
    void organizadorCriaQuestionarioEParticipanteInelegívelNaoResponde() throws Exception {
        String tokenOrg = tokenDe("organizadora@teste.com", "Senha123");

        // cria atividade e questionário via API
        HttpResponse<String> criarEvento = post("/api/eventos",
                "{\"titulo\":\"Ev S7\",\"inicio\":\"2026-12-01T08:00:00\",\"fim\":\"2026-12-01T18:00:00\",\"local\":\"x\"}",
                tokenOrg);
        String eventoId = (String) Json.decodeObjeto(criarEvento.body()).get("id");

        HttpResponse<String> criarAtiv = post("/api/eventos/" + eventoId + "/atividades",
                "{\"titulo\":\"Ativ S7\",\"tipo\":\"PALESTRA\",\"inicio\":\"2026-12-01T09:00:00\",\"fim\":\"2026-12-01T10:00:00\"}", tokenOrg);
        String atividadeId = (String) Json.decodeObjeto(criarAtiv.body()).get("id");

        String qJson = "{\"titulo\":\"Avaliacao\",\"perguntas\":[{\"enunciado\":\"Nota\",\"tipo\":\"ESCALA_NUMERICA\"}]}";
        HttpResponse<String> criarQ = post("/api/atividades/" + atividadeId + "/questionarios", qJson, tokenOrg);
        assertEquals(201, criarQ.statusCode(), criarQ.body());
        String qId = (String) Json.decodeObjeto(criarQ.body()).get("id");

        // participante sem inscrição tenta responder → deve falhar
        post("/api/usuarios", "{\"nome\":\"Nina\",\"email\":\"nina.http@teste.com\",\"senha\":\"Senha123\"}", null);
        String[] nina = loginComId("nina.http@teste.com", "Senha123");
        HttpResponse<String> tentativa = post("/api/questionarios/" + qId + "/respostas",
                "{\"respostas\":{}}", nina[1]);
        assertEquals(400, tentativa.statusCode(), tentativa.body());
    }

    // ---------------- RF-29/30/31: relatórios CSV ----------------

    @Test
    void relatorioInscricoesRetornaCsv() throws Exception {
        String tokenOrg = tokenDe("organizadora@teste.com", "Senha123");

        // ativity já existe no seed do servidor — pegar o id da atividade de 1 vaga
        String atividadeId = atividadeComUmaVagaId();

        HttpResponse<String> resp = get("/api/atividades/" + atividadeId + "/relatorio/inscricoes", tokenOrg);
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.headers().firstValue("content-type").orElse("").contains("text/csv"));
        assertTrue(resp.body().startsWith("atividade;participanteId;nome;email;situacao"));
    }

    @Test
    void relatorioFrequenciaRetornaCsv() throws Exception {
        String tokenOrg = tokenDe("organizadora@teste.com", "Senha123");
        String atividadeId = atividadeComUmaVagaId();

        HttpResponse<String> resp = get("/api/atividades/" + atividadeId + "/relatorio/frequencia", tokenOrg);
        assertEquals(200, resp.statusCode(), resp.body());
        assertTrue(resp.headers().firstValue("content-type").orElse("").contains("text/csv"));
        assertTrue(resp.body().startsWith("atividade;participanteId;nome;totalMarcacoes;presente"));
    }

    @Test
    void relatorioBloqueiaParticipante() throws Exception {
        post("/api/usuarios", "{\"nome\":\"Olga\",\"email\":\"olga.http@teste.com\",\"senha\":\"Senha123\"}", null);
        String tokenPart = tokenDe("olga.http@teste.com", "Senha123");
        String atividadeId = atividadeComUmaVagaId();

        HttpResponse<String> resp = get("/api/atividades/" + atividadeId + "/relatorio/inscricoes", tokenPart);
        assertEquals(403, resp.statusCode());
    }

    // ---------------- auxiliares ----------------

    /** Loga e devolve [id, token] — o token é opaco de propósito, então o id vem do próprio login. */
    private String[] loginComId(String email, String senha) throws Exception {
        HttpResponse<String> resp = post("/api/sessoes", "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}", null);
        assertEquals(200, resp.statusCode(), "login deveria funcionar: " + resp.body());
        Map<String, Object> corpo = Json.decodeObjeto(resp.body());
        return new String[] { (String) corpo.get("id"), (String) corpo.get("token") };
    }

    private String buscarIdInscricao(InscricaoRepositoryEmMemoria inscricoes, String participanteId, String atividadeId) {
        return inscricoes.buscarPorParticipanteEAtividade(UUID.fromString(participanteId), UUID.fromString(atividadeId))
                .orElseThrow().getId().toString();
    }

    private String eventoPublicadoId() {
        return eventos.listarPublicados().get(0).getId().toString();
    }

    private String atividadeComUmaVagaId() {
        return atividades.listarPorEvento(eventos.listarPublicados().get(0).getId()).get(0).getId().toString();
    }

    /** Cria uma atividade nova, com vagas ilimitadas, só para este teste — evita disputa de vaga
     * com outros testes da classe (o servidor e os repositórios são compartilhados entre todos). */
    private String novaAtividadeSemLimiteDeVaga(String titulo) {
        var eventoId = eventos.listarPublicados().get(0).getId();
        var intervalo = new domain.vo.IntervaloTempo(LocalDateTime.now().plusDays(20), LocalDateTime.now().plusDays(20).plusHours(1));
        Atividade a = Atividade.nova(eventoId, titulo, Atividade.TipoAtividade.PALESTRA, intervalo, domain.vo.Vagas.ilimitadas());
        atividades.salvar(a);
        return a.getId().toString();
    }
}
