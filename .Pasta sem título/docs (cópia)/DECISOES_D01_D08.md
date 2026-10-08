# Decisões arquiteturais D-01 a D-08

Documento exigido pela seção 11.1 da especificação. Para cada decisão: problema, alternativa
descartada e efeito no projeto.

---

## D-01 — Interface desktop

**Decisão:** nenhuma interface desktop proprietária foi implementada. A API HTTP (`app.ServidorApi`)
serve tanto o site público quanto qualquer cliente futuro (desktop, mobile ou script). O site
estático (`site/index.html`) faz o papel de cliente de demonstração.

**Alternativa descartada:** JavaFX ou Swing. Descartadas porque (a) acrescentam dependência sem
ganho de avaliação — a especificação avalia o modelo, não a tela; (b) forçariam dois pontos de
entrada com lógica duplicada; (c) a API HTTP já cumpre RNF-02 ("desktop e site consomem a mesma
API e o mesmo banco").

**Efeito:** toda lógica de negócio fica em um só lugar. Trocar o cliente (JavaFX, Swing, React)
é só trocar o consumidor da API — o domínio não muda.

---

## D-02 — Framework de API e autenticação

**Decisão:** `com.sun.net.httpserver.HttpServer` (parte do JDK, sem dependência externa) + token
opaco em `TokenStore` (UUID em memória).

**Alternativa descartada:** Spring Boot. Descartada porque (a) adiciona 40+ MB de dependências;
(b) oculta o roteamento e a injeção de dependências, dificultando a explicação na prova oral;
(c) o projeto tem 7 rotas — a complexidade de Spring não se justifica.

**Alternativa descartada:** JWT. Descartada porque JWT não foi pedido pela especificação e
adicionaria lógica de assinatura/verificação sem ganho de avaliação. O `TokenStore` em memória
cumpre RF-02 e RNF-06 no escopo da disciplina.

**Efeito:** o código do servidor cabe em uma classe, é legível, e cada rota é explicável linha
a linha na prova oral.

---

## D-03 — Banco de dados, mapeamento e migração

**Decisão:** PostgreSQL (Supabase) com JDBC puro e SQL explícito nos adaptadores. Migrations
manuais em `Database/`.

**Alternativa descartada:** JPA/Hibernate. Descartada porque (a) oculta o SQL e torna difícil
explicar o mapeamento objeto-relacional; (b) puxaria anotações de infraestrutura para as
entidades do domínio, violando ROO-09 (domínio independente de framework); (c) o banco tem 5
tabelas — a complexidade de JPA não se justifica.

**Efeito:** cada adaptador de banco é auto-contido e testável isoladamente. Trocar o banco é
trocar os adaptadores; o domínio não sabe que existe SQL.

---

## D-04 — Tecnologia do site e consumo da API

**Decisão:** HTML puro + JavaScript com `fetch()`. Nenhum framework de front-end.

**Alternativa descartada:** React ou Vue. Descartados porque (a) exigem build (Node, npm, webpack),
dificultando execução em outro computador (RNF-15); (b) o site é deliberadamente simples — a
especificação avalia o backend, não a UI.

**Efeito:** o site abre com duplo clique, sem servidor, sem build. O `API_BASE` no topo do
`<script>` é o único ponto a mudar se a porta mudar.

---

## D-05 — QR Code: geração, validade e prevenção de duplicidade

**Decisão:** o código do QR é um UUID gerado pelo `TokenStore` (separado do `TokenStore` de
sessão — RN-10: "não deve conter senha nem dado pessoal sensível"). O site desenha o QR com
`qrcode-generator` (MIT, vendorizado em `site/vendor/`). Não há expiração automática do código
nesta versão.

**Alternativa descartada:** gerar um JWT assinado com payload `{participanteId, atividadeId,
exp}`. Descartada pelo mesmo motivo do D-02 (complexidade sem ganho proporcional) e porque
tornaria o código QR dependente do algoritmo de assinatura — difícil de trocar depois.

**Limitação declarada:** o código não expira (sobrevive até o servidor reiniciar). Para
produção, o `TokenStore` precisaria de TTL ou de persistência. Fora do escopo obrigatório.

---

## D-06 — Políticas configuráveis de inscrição, frequência, avaliação e certificado

| Aspecto | Decisão | Alternativa descartada |
|---|---|---|
| Conflito de inscrição (RN-07) | `PoliticaConflitoHorario` (Strategy) — `Bloqueante` ou `AlertaSomente` | Flag booleana no caso de uso — cada nova regra vira mais um `if` |
| Critério de frequência (RF-19) | `CriterioFrequencia` (enum com Strategy embutido) — `CHECK_IN_UNICO`, `ENTRADA_SAIDA`, `MANUAL` | `switch` espalhado pelos casos de uso |
| Validação de resposta (RF-25) | `TipoPergunta` (enum com `validarResposta()`) | Hierarquia de subclasses por tipo |
| Certificado | Fora do escopo desta versão (Desejável D) | — |

**Efeito comum:** acrescentar uma nova política/critério/tipo é acrescentar uma constante ou
uma classe; nenhum caso de uso existente muda.

---

## D-07 — Padrões de projeto aplicados

| Padrão | Onde | Problema resolvido | Alternativa descartada |
|---|---|---|---|
| **Strategy** | `PoliticaConflitoHorario` | RN-07: conflito pode bloquear ou só avisar — a escolha não deve estar dentro do caso de uso | `if (bloquear) throw else avisos.add(...)` dentro do caso de uso |
| **Strategy** (enum) | `CriterioFrequencia`, `TipoPergunta` | Diferentes regras de validação por tipo, sem `if/switch` no chamador | `switch(tipo)` em cada ponto de uso |
| **Factory Method** | `RespostaQuestionario.criar(...)` | Validar todas as respostas antes de criar qualquer objeto (atomicidade) | Construtor público que valida parcialmente |
| **Repository** | Todas as portas de saída | Isolar o domínio de SQL/memória — os casos de uso não sabem como os dados são guardados | DAO com SQL direto nos casos de uso |
| **Ports and Adapters** (Hexagonal) | Toda a arquitetura | Manter domínio independente de banco, HTTP e site | Camadas tradicionais (Controller → Service → DAO) com dependências diretas |

---

## D-08 — Testes, refatorações e dados de demonstração

**Testes:** JUnit 5. Três camadas:
1. **Domínio puro** (`domain/model/`, `domain/vo/`, `domain/model/avaliacao/`) — sem dependência de banco ou HTTP.
2. **Casos de uso com repositórios em memória** (`application/`) — sem banco, sem HTTP.
3. **HTTP de ponta a ponta** (`app/ServidorApiTest`) — sobe `HttpServer` real numa porta livre, usa `java.net.http.HttpClient`.
4. **Integração com banco real** (`infrastructure/PersistenciaIntegracaoTest`) — só roda com `DB_PASSWORD` definida.

**Refatorações documentadas:**

| Rodada | Antes | Depois | Efeito |
|---|---|---|---|
| S2→S4 | `RealizarInscricaoUseCase` calculava conflito internamente com `Inscricao.getAtividadeId()` comparado dentro do caso de uso | `Atividade.conflitaCom()` + `IntervaloTempo.temConflitoCom()` — a regra mora no objeto que a conhece | Teste `IntervaloTempoTest` passa a cobrir a regra diretamente |
| S4 | `total_inscritos` não era salvo de volta no banco após incremento | `atividadeRepository.salvar(atividade)` após `incrementarInscrito()` | Bug só apareceu com banco real — argumento vivo para testes de integração |
| S6 | `RegistroPresenca` usava `LocalDateTime.now()` na reconstituição | Segundo construtor com `dataHora` explícita | Registros antigos voltam com a data/hora real do banco |
| S7 | Lógica de formatação CSV inline nas rotas (potencial) | `RelatorioExportavel` + implementadores — rota só chama `toCsv()` | Adicionar PDF é criar uma nova classe, sem tocar rota nem caso de uso |

**Dados de demonstração:** `app/SeedDemo.java` (cria 500 participantes e 100 atividades pelos
próprios casos de uso, não por INSERT direto). `app/DemoFinal.java` (CA-01 a CA-07 em memória,
sem banco).
