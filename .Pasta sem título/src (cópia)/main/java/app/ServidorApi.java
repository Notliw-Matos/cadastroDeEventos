package app;

import application.*;
import application.ports.in.*;
import domain.exception.AcessoNegadoException;
import domain.exception.CredenciaisInvalidasException;
import domain.exception.DominioException;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Atividade;
import domain.model.Evento;
import domain.model.Usuario;
import domain.ports.AtividadeRepository;
import domain.ports.EventoRepository;
import domain.ports.InscricaoRepository;
import domain.ports.UsuarioRepository;
import domain.strategy.PoliticaConflitoHorario;
import domain.strategy.PoliticaConflitoBloqueante;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import infrastructure.http.Json;
import infrastructure.http.RequisicaoInvalidaException;
import infrastructure.http.TokenStore;
import infrastructure.memoria.AtividadeRepositoryEmMemoria;
import infrastructure.memoria.EventoRepositoryEmMemoria;
import infrastructure.memoria.InscricaoRepositoryEmMemoria;
import infrastructure.memoria.UsuarioRepositoryEmMemoria;
import infrastructure.security.BCryptPasswordHasher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * S4/S5 — Adaptador de entrada (API REST) e composition root do servidor.
 *
 * Sem framework: usa só com.sun.net.httpserver.HttpServer (parte do JDK). É a mesma API para o
 * site público (RNF-02); a aplicação desktop, quando existir, também deve falar com esta API,
 * nunca acessar o banco direto (D-04).
 *
 * Roda com banco real se DB_URL/DB_USER/DB_PASSWORD estiverem definidos; senão roda em memória,
 * com dados de demonstração recriados a cada início (ver docs/S4_ENTREGA.md).
 */
public class ServidorApi {

    private static final Pattern ROTA_ATIVIDADES_DO_EVENTO = Pattern.compile("^/api/eventos/([^/]+)/atividades$");
    private static final Pattern ROTA_SITUACAO_PRESENCA = Pattern.compile("^/api/frequencia/([^/]+)/situacao$");

    public static void main(String[] args) throws IOException {
        int porta = Integer.parseInt(System.getenv().getOrDefault("API_PORT", "8080"));
        boolean usarBanco = infrastructure.persistence.ConexaoDatabase.estaConfigurado();

        UsuarioRepository usuarios;
        EventoRepository eventos;
        AtividadeRepository atividades;
        InscricaoRepository inscricoes;
        domain.ports.RegistroFrequenciaRepository registrosFrequencia;
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        UUID organizadorDemoId = null;

        if (usarBanco) {
            usuarios = new infrastructure.persistence.UsuarioRepositoryDatabase();
            eventos = new infrastructure.persistence.EventoRepositoryDatabase();
            atividades = new infrastructure.persistence.AtividadeRepositoryDatabase();
            inscricoes = new infrastructure.persistence.InscricaoRepositoryDatabase();
            registrosFrequencia = new infrastructure.persistence.RegistroFrequenciaRepositoryDatabase();
            System.out.println("Modo: BANCO (DB_URL configurada) - dados persistem de verdade.");
        } else {
            usuarios = new UsuarioRepositoryEmMemoria();
            eventos = new EventoRepositoryEmMemoria();
            atividades = new AtividadeRepositoryEmMemoria();
            inscricoes = new InscricaoRepositoryEmMemoria();
            registrosFrequencia = new infrastructure.memoria.RegistroFrequenciaRepositoryEmMemoria();
            organizadorDemoId = semearDadosDeDemonstracao(usuarios, eventos, atividades, hasher);
            System.out.println("Modo: EM MEMORIA (defina DB_URL/DB_USER/DB_PASSWORD para usar o Supabase) - dados somem ao reiniciar.");
        }

        HttpServer servidor = criar(porta, usuarios, eventos, atividades, inscricoes, registrosFrequencia,
                hasher, new TokenStore());
        servidor.start();

        System.out.println("API rodando em http://127.0.0.1:" + porta);
        if (organizadorDemoId != null) {
            System.out.println("Organizador de demonstracao: organizador@demo.com / Senha123 (id " + organizadorDemoId + ")");
        }
        System.out.println("Abra site/index.html no navegador para usar o site publico.");
    }

    /**
     * Monta o HttpServer com todas as rotas já registradas (mas ainda não iniciado — chame
     * .start()). Extraído do main() para poder ser reaproveitado pelos testes de integração
     * HTTP, que sobem o servidor numa porta aleatória dentro da própria JVM de teste.
     */
    public static HttpServer criar(int porta, UsuarioRepository usuarios, EventoRepository eventos,
                                    AtividadeRepository atividades, InscricaoRepository inscricoes,
                                    domain.ports.RegistroFrequenciaRepository registrosFrequencia,
                                    BCryptPasswordHasher hasher, TokenStore tokenStore) throws IOException {
        return criar(porta, usuarios, eventos, atividades, inscricoes, registrosFrequencia, hasher, tokenStore,
                new PoliticaConflitoBloqueante());
    }

    /** Variante que permite escolher a política de conflito de horário (S5) — ver docs/S5_ENTREGA.md. */
    public static HttpServer criar(int porta, UsuarioRepository usuarios, EventoRepository eventos,
                                    AtividadeRepository atividades, InscricaoRepository inscricoes,
                                    domain.ports.RegistroFrequenciaRepository registrosFrequencia,
                                    BCryptPasswordHasher hasher, TokenStore tokenStore,
                                    PoliticaConflitoHorario politicaConflito) throws IOException {
        RegistrarPresencaUseCase registrarPresenca =
                new RegistrarPresencaUseCase(inscricoes, atividades, eventos, usuarios, registrosFrequencia);

        domain.ports.QuestionarioRepository questionarios =
                new infrastructure.memoria.QuestionarioRepositoryEmMemoria();
        domain.ports.RespostaQuestionarioRepository respostas =
                new infrastructure.memoria.RespostaQuestionarioRepositoryEmMemoria();

        Dependencias dep = new Dependencias(tokenStore, new TokenStore(), usuarios, inscricoes,
                new CadastrarUsuarioUseCase(usuarios, hasher),
                new AutenticarUsuarioUseCase(usuarios, hasher),
                new CriarEventoUseCase(eventos, usuarios),
                new CriarAtividadeUseCase(atividades, eventos, usuarios),
                new RealizarInscricaoUseCase(inscricoes, atividades, politicaConflito),
                new ListarEventosPublicosUseCase(eventos),
                new ListarAtividadesDoEventoUseCase(atividades, eventos),
                new VerAgendaUseCase(inscricoes, atividades),
                registrarPresenca,
                new CriarQuestionarioUseCase(questionarios, atividades, eventos, usuarios),
                new ResponderQuestionarioUseCase(respostas, questionarios, inscricoes, registrosFrequencia, registrarPresenca),
                new ConsultarConsolidacaoUseCase(questionarios, respostas, atividades, eventos, usuarios),
                new GerarRelatorioUseCase(inscricoes, usuarios, atividades, eventos, registrosFrequencia));

        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", porta), 0);
        servidor.createContext("/api/", exchange -> despachar(exchange, dep));
        servidor.setExecutor(null);
        return servidor;
    }

    /** Agrupa todos os casos de uso que as rotas chamam — evita uma lista gigante de parâmetros. */
    private static final class Dependencias {
        final TokenStore tokenStore;
        final TokenStore qrTokenStore; // RF-20: pool separado do de sessão — um código de QR vazado não vira login
        final UsuarioRepository usuarios;
        final InscricaoRepository inscricoes;
        final CadastrarUsuario cadastrar;
        final AutenticarUsuario autenticar;
        final CriarEvento criarEvento;
        final CriarAtividade criarAtividade;
        final RealizarInscricaoUseCase realizarInscricao;
        final ListarEventosPublicos listarEventos;
        final ListarAtividadesDoEvento listarAtividades;
        final VerAgenda verAgenda;
        final RegistrarPresencaUseCase registrarPresenca;
        final CriarQuestionarioUseCase criarQuestionario;
        final ResponderQuestionarioUseCase responderQuestionario;
        final ConsultarConsolidacaoUseCase consultarConsolidacao;
        final GerarRelatorioUseCase gerarRelatorio;

        Dependencias(TokenStore tokenStore, TokenStore qrTokenStore, UsuarioRepository usuarios,
                     InscricaoRepository inscricoes, CadastrarUsuario cadastrar, AutenticarUsuario autenticar,
                     CriarEvento criarEvento, CriarAtividade criarAtividade, RealizarInscricaoUseCase realizarInscricao,
                     ListarEventosPublicos listarEventos, ListarAtividadesDoEvento listarAtividades, VerAgenda verAgenda,
                     RegistrarPresencaUseCase registrarPresenca,
                     CriarQuestionarioUseCase criarQuestionario, ResponderQuestionarioUseCase responderQuestionario,
                     ConsultarConsolidacaoUseCase consultarConsolidacao, GerarRelatorioUseCase gerarRelatorio) {
            this.tokenStore = tokenStore;
            this.qrTokenStore = qrTokenStore;
            this.usuarios = usuarios;
            this.inscricoes = inscricoes;
            this.cadastrar = cadastrar;
            this.autenticar = autenticar;
            this.criarEvento = criarEvento;
            this.criarAtividade = criarAtividade;
            this.realizarInscricao = realizarInscricao;
            this.listarEventos = listarEventos;
            this.listarAtividades = listarAtividades;
            this.verAgenda = verAgenda;
            this.registrarPresenca = registrarPresenca;
            this.criarQuestionario = criarQuestionario;
            this.responderQuestionario = responderQuestionario;
            this.consultarConsolidacao = consultarConsolidacao;
            this.gerarRelatorio = gerarRelatorio;
        }
    }

    // ---------------- roteamento ----------------

    private static void despachar(HttpExchange exchange, Dependencias dep) throws IOException {
        String metodo = exchange.getRequestMethod();
        String caminho = exchange.getRequestURI().getPath();

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

        if ("OPTIONS".equals(metodo)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            Resposta resposta = rotear(metodo, caminho, exchange, dep);
            if (resposta.status >= 0) escrever(exchange, resposta.status, resposta.corpo);
        } catch (RequisicaoInvalidaException e) {
            escrever(exchange, e.getCodigoHttp(), erro(e.getMessage()));
        } catch (AcessoNegadoException e) {
            escrever(exchange, 403, erro(e.getMessage()));
        } catch (CredenciaisInvalidasException e) {
            escrever(exchange, 401, erro(e.getMessage()));
        } catch (RecursoNaoEncontradoException e) {
            escrever(exchange, 404, erro(e.getMessage()));
        } catch (DominioException e) {
            escrever(exchange, 400, erro(e.getMessage()));
        } catch (Exception e) {
            escrever(exchange, 500, erro("Erro interno: " + e.getMessage()));
        }
    }

    private static Resposta rotear(String metodo, String caminho, HttpExchange exchange, Dependencias dep) throws Exception {
        Matcher m;

        // ---- RF-01: cadastro público ----
        if ("POST".equals(metodo) && "/api/usuarios".equals(caminho)) {
            Map<String, Object> corpo = lerCorpo(exchange);
            UsuarioResumo criado = dep.cadastrar.executar(texto(corpo, "nome"), texto(corpo, "email"), texto(corpo, "senha"));
            return new Resposta(201, mapaUsuario(criado));
        }

        // ---- RF-02: login ----
        if ("POST".equals(metodo) && "/api/sessoes".equals(caminho)) {
            Map<String, Object> corpo = lerCorpo(exchange);
            UsuarioResumo usuario = dep.autenticar.executar(texto(corpo, "email"), texto(corpo, "senha"));
            String token = dep.tokenStore.emitir(usuario.getId());
            Map<String, Object> resposta = mapaUsuario(usuario);
            resposta.put("token", token);
            return new Resposta(200, resposta);
        }

        // ---- RF-10: eventos publicados (site público, sem autenticação) ----
        if ("GET".equals(metodo) && "/api/eventos".equals(caminho)) {
            List<Map<String, Object>> lista = dep.listarEventos.executar().stream()
                    .map(EventoPublicoDTO::paraMapa).collect(Collectors.toList());
            return new Resposta(200, lista);
        }

        // ---- RF-04: criar evento (autenticado) ----
        if ("POST".equals(metodo) && "/api/eventos".equals(caminho)) {
            UUID solicitanteId = autenticado(exchange, dep.tokenStore);
            Map<String, Object> corpo = lerCorpo(exchange);
            IntervaloTempo periodo = new IntervaloTempo(
                    LocalDateTime.parse(texto(corpo, "inicio")), LocalDateTime.parse(texto(corpo, "fim")));
            Evento evento = dep.criarEvento.executar(solicitanteId, texto(corpo, "titulo"),
                    (String) corpo.get("descricao"), periodo, (String) corpo.get("local"));
            return new Resposta(201, mapaEvento(evento));
        }

        // ---- RF-09: atividades de um evento (site público) ----
        m = ROTA_ATIVIDADES_DO_EVENTO.matcher(caminho);
        if ("GET".equals(metodo) && m.matches()) {
            List<Map<String, Object>> lista = dep.listarAtividades.executar(UUID.fromString(m.group(1))).stream()
                    .map(AtividadeDTO::paraMapa).collect(Collectors.toList());
            return new Resposta(200, lista);
        }

        // ---- RF-05: criar atividade (autenticado) ----
        m = ROTA_ATIVIDADES_DO_EVENTO.matcher(caminho);
        if ("POST".equals(metodo) && m.matches()) {
            UUID solicitanteId = autenticado(exchange, dep.tokenStore);
            UUID eventoId = UUID.fromString(m.group(1));
            Map<String, Object> corpo = lerCorpo(exchange);
            IntervaloTempo periodo = new IntervaloTempo(
                    LocalDateTime.parse(texto(corpo, "inicio")), LocalDateTime.parse(texto(corpo, "fim")));
            Vagas vagas = corpo.get("vagas") == null ? Vagas.ilimitadas() : Vagas.limitadas(numero(corpo, "vagas").intValue());
            Atividade.TipoAtividade tipo = Atividade.TipoAtividade.valueOf(texto(corpo, "tipo"));
            Atividade atividade = dep.criarAtividade.executar(solicitanteId, eventoId, texto(corpo, "titulo"), tipo, periodo, vagas);
            return new Resposta(201, mapaAtividade(atividade));
        }

        // ---- RF-12: inscrição (autenticado) ----
        if ("POST".equals(metodo) && "/api/inscricoes".equals(caminho)) {
            UUID participanteId = autenticado(exchange, dep.tokenStore);
            Map<String, Object> corpo = lerCorpo(exchange);
            UUID atividadeId = UUID.fromString(texto(corpo, "atividadeId"));
            ResultadoInscricao resultado = dep.realizarInscricao.executar(participanteId, atividadeId);
            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("id", resultado.getInscricao().getId().toString());
            resposta.put("situacao", resultado.getInscricao().getSituacao().name());
            resposta.put("avisos", resultado.getAvisos()); // RF-18: avisos de conflito, se a política for "alerta"
            return new Resposta(201, resposta);
        }

        // ---- RF-16, RF-17: agenda pessoal (autenticado) ----
        if ("GET".equals(metodo) && "/api/agenda".equals(caminho)) {
            UUID participanteId = autenticado(exchange, dep.tokenStore);
            List<Map<String, Object>> lista = dep.verAgenda.executar(participanteId).stream()
                    .map(ItemAgendaDTO::paraMapa).collect(Collectors.toList());
            return new Resposta(200, lista);
        }

        // ---- RF-20: gera o código que vira o QR pessoal de presença (autenticado) ----
        if ("GET".equals(metodo) && "/api/frequencia/meu-codigo".equals(caminho)) {
            UUID participanteId = autenticado(exchange, dep.tokenStore);
            String codigo = dep.qrTokenStore.emitir(participanteId); // RN-10: código opaco, sem senha nem dado sensível
            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("codigo", codigo);
            return new Resposta(200, resposta);
        }

        // ---- RF-21: registrar presença lendo o código do QR (organizador do evento, ou admin) ----
        if ("POST".equals(metodo) && "/api/frequencia/checkin".equals(caminho)) {
            UUID operadorId = autenticado(exchange, dep.tokenStore);
            Map<String, Object> corpo = lerCorpo(exchange);
            UUID participanteId = dep.qrTokenStore.resolver(texto(corpo, "codigo"));
            if (participanteId == null) {
                throw new RequisicaoInvalidaException(400, "Código inválido ou expirado. Peça para a pessoa gerar um novo.");
            }
            UUID atividadeId = UUID.fromString(texto(corpo, "atividadeId"));
            var tipo = domain.model.RegistroPresenca.TipoMarcacao.valueOf(texto(corpo, "tipo"));
            return registrarEResponder(dep, participanteId, atividadeId, tipo, operadorId);
        }

        // ---- RF-22: lançamento manual alternativo, sem QR (organizador do evento, ou admin) ----
        if ("POST".equals(metodo) && "/api/frequencia/manual".equals(caminho)) {
            UUID operadorId = autenticado(exchange, dep.tokenStore);
            Map<String, Object> corpo = lerCorpo(exchange);
            UUID participanteId = UUID.fromString(texto(corpo, "participanteId"));
            UUID atividadeId = UUID.fromString(texto(corpo, "atividadeId"));
            return registrarEResponder(dep, participanteId, atividadeId, domain.model.RegistroPresenca.TipoMarcacao.MANUAL, operadorId);
        }

        // ---- RF-23: situação de presença calculada (autenticado) ----
        m = ROTA_SITUACAO_PRESENCA.matcher(caminho);
        if ("GET".equals(metodo) && m.matches()) {
            autenticado(exchange, dep.tokenStore); // só exige login; não restringe a dono da inscrição (ver docs/S6_ENTREGA.md)
            UUID inscricaoId = UUID.fromString(m.group(1));
            boolean presente = dep.registrarPresenca.calcularSituacaoDePresenca(inscricaoId);
            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("presente", presente);
            return new Resposta(200, resposta);
        }

        // ---- RF-24: criar questionário (autenticado, organizador do evento) ----
        m = Pattern.compile("^/api/atividades/([^/]+)/questionarios$").matcher(caminho);
        if ("POST".equals(metodo) && m.matches()) {
            UUID solicitanteId = autenticado(exchange, dep.tokenStore);
            UUID atividadeId = UUID.fromString(m.group(1));
            Map<String, Object> corpo = lerCorpo(exchange);
            List<domain.model.avaliacao.Pergunta> perguntas = parsePerguntasDoCorpo(corpo);
            domain.model.avaliacao.Questionario q = dep.criarQuestionario.executar(
                    solicitanteId, atividadeId, texto(corpo, "titulo"), perguntas);
            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("id", q.getId().toString());
            resposta.put("titulo", q.getTitulo());
            resposta.put("totalPerguntas", q.getPerguntas().size());
            return new Resposta(201, resposta);
        }

        // ---- RF-26: responder questionário (autenticado, participante inscrito e presente) ----
        m = Pattern.compile("^/api/questionarios/([^/]+)/respostas$").matcher(caminho);
        if ("POST".equals(metodo) && m.matches()) {
            UUID participanteId = autenticado(exchange, dep.tokenStore);
            UUID questionarioId = UUID.fromString(m.group(1));
            Map<String, Object> corpo = lerCorpo(exchange);
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = (Map<String, Object>) corpo.getOrDefault("respostas", new LinkedHashMap<>());
            Map<UUID, String> valoresPorPergunta = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : raw.entrySet())
                valoresPorPergunta.put(UUID.fromString(e.getKey()), e.getValue().toString());
            domain.model.avaliacao.RespostaQuestionario r =
                    dep.responderQuestionario.executar(participanteId, questionarioId, valoresPorPergunta);
            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("id", r.getId().toString());
            resposta.put("questionarioId", r.getQuestionarioId().toString());
            return new Resposta(201, resposta);
        }

        // ---- RF-28: consolidação (autenticado, organizador) ----
        m = Pattern.compile("^/api/questionarios/([^/]+)/consolidacao$").matcher(caminho);
        if ("GET".equals(metodo) && m.matches()) {
            UUID solicitanteId = autenticado(exchange, dep.tokenStore);
            UUID questionarioId = UUID.fromString(m.group(1));
            ConsolidacaoAvaliacaoDTO consolid = dep.consultarConsolidacao.executar(solicitanteId, questionarioId);
            return new Resposta(200, consolid.paraMapa());
        }

        // ---- RF-29/30/31: relatórios em CSV (autenticado, organizador) ----
        m = Pattern.compile("^/api/atividades/([^/]+)/relatorio/(inscricoes|frequencia)$").matcher(caminho);
        if ("GET".equals(metodo) && m.matches()) {
            UUID solicitanteId = autenticado(exchange, dep.tokenStore);
            UUID atividadeId = UUID.fromString(m.group(1));
            GerarRelatorioUseCase.TipoRelatorio tipo = "inscricoes".equals(m.group(2))
                    ? GerarRelatorioUseCase.TipoRelatorio.INSCRICOES
                    : GerarRelatorioUseCase.TipoRelatorio.FREQUENCIA;
            application.relatorio.RelatorioExportavel rel = dep.gerarRelatorio.executar(solicitanteId, atividadeId, tipo);
            byte[] bytes = rel.toCsv().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + rel.nomeArquivo() + "\"");
            exchange.sendResponseHeaders(200, bytes.length);
            try (java.io.OutputStream out = exchange.getResponseBody()) { out.write(bytes); }
            return new Resposta(-1, null); // -1 = já escrito manualmente, despachar() vai ignorar
        }

        throw new RequisicaoInvalidaException(404, "Rota não encontrada: " + metodo + " " + caminho);
    }

    @SuppressWarnings("unchecked")
    private static List<domain.model.avaliacao.Pergunta> parsePerguntasDoCorpo(Map<String, Object> corpo) {
        List<Object> raw = (List<Object>) corpo.getOrDefault("perguntas", new java.util.ArrayList<>());
        List<domain.model.avaliacao.Pergunta> lista = new java.util.ArrayList<>();
        for (Object obj : raw) {
            Map<String, Object> p = (Map<String, Object>) obj;
            String enunciado = p.get("enunciado").toString();
            domain.model.avaliacao.TipoPergunta tipo =
                    domain.model.avaliacao.TipoPergunta.valueOf(p.get("tipo").toString());
            List<String> opcoes = new java.util.ArrayList<>();
            if (p.containsKey("opcoes")) {
                for (Object op : (List<Object>) p.get("opcoes")) opcoes.add(op.toString());
            }
            lista.add(new domain.model.avaliacao.Pergunta(null, enunciado, tipo, opcoes));
        }
        if (lista.isEmpty()) throw new RequisicaoInvalidaException(400, "O questionário precisa de pelo menos uma pergunta.");
        return lista;
    }

    /** RF-20/21/22 convergem aqui: acham a inscrição de (participante, atividade) e registram. */
    private static Resposta registrarEResponder(Dependencias dep, UUID participanteId, UUID atividadeId,
                                                 domain.model.RegistroPresenca.TipoMarcacao tipo, UUID operadorId) {
        UUID inscricaoId = dep.inscricoes.buscarPorParticipanteEAtividade(participanteId, atividadeId)
                .map(domain.model.Inscricao::getId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Este participante não está inscrito nesta atividade."));
        var registro = dep.registrarPresenca.registrarMarcacao(inscricaoId, tipo, operadorId);
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("id", registro.getId().toString());
        resposta.put("tipo", registro.getTipo().name());
        resposta.put("dataHora", registro.getDataHora().toString());
        return new Resposta(201, resposta);
    }

    // ---------------- autenticação da requisição (RNF-06: verificado no servidor) ----------------

    private static UUID autenticado(HttpExchange exchange, TokenStore tokenStore) {
        String cabecalho = exchange.getRequestHeaders().getFirst("Authorization");
        String token = (cabecalho != null && cabecalho.startsWith("Bearer ")) ? cabecalho.substring(7) : null;
        UUID usuarioId = tokenStore.resolver(token);
        if (usuarioId == null) {
            throw new RequisicaoInvalidaException(401, "Não autenticado. Envie 'Authorization: Bearer <token>' do login.");
        }
        return usuarioId;
    }

    // ---------------- leitura/escrita HTTP ----------------

    private static Map<String, Object> lerCorpo(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] pedaco = new byte[4096];
            int lidos;
            while ((lidos = in.read(pedaco)) != -1) buffer.write(pedaco, 0, lidos);
            String texto = buffer.toString(StandardCharsets.UTF_8);
            if (texto.isBlank()) return new LinkedHashMap<>();
            try {
                return Json.decodeObjeto(texto);
            } catch (RuntimeException e) {
                throw new RequisicaoInvalidaException(400, "Corpo da requisição não é um JSON válido.");
            }
        }
    }

    private static void escrever(HttpExchange exchange, int status, Object corpo) throws IOException {
        byte[] bytes = Json.encode(corpo).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static String texto(Map<String, Object> corpo, String campo) {
        Object valor = corpo.get(campo);
        if (valor == null) throw new RequisicaoInvalidaException(400, "Campo obrigatório ausente: " + campo);
        return valor.toString();
    }

    private static Double numero(Map<String, Object> corpo, String campo) {
        Object valor = corpo.get(campo);
        if (!(valor instanceof Number)) throw new RequisicaoInvalidaException(400, "Campo numérico inválido: " + campo);
        return ((Number) valor).doubleValue();
    }

    private static Map<String, Object> erro(String mensagem) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("mensagem", mensagem);
        return mapa;
    }

    private static Map<String, Object> mapaUsuario(UsuarioResumo usuario) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", usuario.getId().toString());
        mapa.put("nome", usuario.getNome());
        mapa.put("email", usuario.getEmail());
        mapa.put("perfil", usuario.getPerfil().name());
        return mapa;
    }

    private static Map<String, Object> mapaEvento(Evento evento) {
        Map<String, Object> mapa = EventoPublicoDTO.de(evento).paraMapa();
        mapa.put("estado", evento.getEstado().name());
        return mapa;
    }

    private static Map<String, Object> mapaAtividade(Atividade atividade) {
        return AtividadeDTO.de(atividade).paraMapa();
    }

    private static final class Resposta {
        final int status;
        final Object corpo;

        Resposta(int status, Object corpo) {
            this.status = status;
            this.corpo = corpo;
        }
    }

    // ---------------- dados de demonstração ----------------

    private static UUID semearDadosDeDemonstracao(UsuarioRepository usuarios, EventoRepository eventos,
                                                    AtividadeRepository atividades, BCryptPasswordHasher hasher) {
        Usuario organizador = Usuario.novoParticipante("Organizador Demo",
                new domain.vo.Email("organizador@demo.com"), hasher.hash("Senha123"));
        organizador = new Usuario(organizador.getId(), organizador.getNome(), organizador.getEmail(),
                organizador.getSenhaHash(), Usuario.Perfil.ORGANIZADOR);
        usuarios.salvar(organizador);

        LocalDateTime inicio = LocalDateTime.now().plusDays(20).withHour(8).withMinute(0).withSecond(0).withNano(0);
        Evento evento = Evento.novo(organizador.getId(), "Semana de Tecnologia 2026",
                "Evento de demonstração criado ao iniciar o servidor.",
                new IntervaloTempo(inicio, inicio.plusHours(10)), "Auditório Principal");
        evento.publicar();
        eventos.salvar(evento);

        Atividade palestra = Atividade.nova(evento.getId(), "Palestra de abertura: IA e POO",
                Atividade.TipoAtividade.PALESTRA, new IntervaloTempo(inicio, inicio.plusHours(1)), Vagas.limitadas(100));
        Atividade oficina = Atividade.nova(evento.getId(), "Oficina prática de testes",
                Atividade.TipoAtividade.OFICINA, new IntervaloTempo(inicio.plusHours(2), inicio.plusHours(4)), Vagas.limitadas(30));
        atividades.salvar(palestra);
        atividades.salvar(oficina);

        return organizador.getId();
    }
}
