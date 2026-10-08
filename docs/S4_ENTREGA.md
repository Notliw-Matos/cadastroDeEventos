# Entrega S4 — Adaptadores, site público e inscrição

## 0. Migração de banco (rode uma vez no SQL Editor do Supabase)

O schema real (confirmado em 30/09) não tinha `organizador_id` nem `estado` em `eventos`
(obrigatórios para RN-18 e RN-04), nem `criterio_frequencia` em `atividades` (vai ser usado em
S6). Rode isto uma vez, no SQL Editor do seu projeto:

```sql
-- 1) apaga o evento de teste (não tem dono, então não daria pra preencher organizador_id)
delete from eventos where titulo = 'Evento de Teste';

-- 2) eventos: dono e estado
alter table eventos
  add column organizador_id uuid not null references usuarios(id),
  add column estado varchar not null default 'RASCUNHO';

-- 3) atividades: critério de frequência (S6) e trilha/categoria (RF-06, opcional)
alter table atividades
  add column criterio_frequencia varchar not null default 'CHECK_IN_UNICO',
  add column trilha_categoria varchar;

-- 4) índices para as consultas mais comuns (RNF-10)
create index if not exists idx_atividades_evento on atividades(evento_id);
create index if not exists idx_inscricoes_participante on inscricoes(participante_id);
create index if not exists idx_inscricoes_atividade on inscricoes(atividade_id);
```

Se o passo 2 der erro de "column organizador_id contains null values" é porque ainda existe
alguma linha em `eventos` sem dono — apague-a ou rode `update eventos set organizador_id =
'<algum id de usuarios>'` antes de repetir o `alter table`.

## 1. Como executar

**Passo 1 — configurar a conexão com o banco (opcional; sem isso a API roda em memória):**
Defina três variáveis de ambiente — nenhuma senha vai para o código:

| Variável | Onde encontrar |
|---|---|
| `DB_URL` | Painel do Supabase → Project Settings → Database → Connection string → aba **Session pooler**. Pegue o host e a porta e monte `jdbc:postgresql://HOST:PORTA/postgres` |
| `DB_USER` | Mesmo lugar (algo como `postgres.jrqlzhxbpfodteflnypu`) |
| `DB_PASSWORD` | A senha do banco que a equipe definiu ao criar o projeto (troquem se `PROJETOPOO2026` ainda estiver valendo — ela vazou no histórico do Git) |

No Eclipse: *Run Configurations → aba Environment → New* para cada uma. Na linha de comando:
`export DB_URL=... DB_USER=... DB_PASSWORD=...` (Linux/Mac/Git Bash) ou `set DB_URL=...` (cmd).

**Passo 2 — (se for usar banco) carregar dados de demonstração, uma vez:**
`scripts\seed.bat` ou `./scripts/seed.sh`. Cria 500 participantes, 1 evento publicado e 100
atividades (RNF-10) — sempre pelos mesmos casos de uso da aplicação, não por INSERT direto.

**Passo 3 — subir a API:**
- Eclipse: rodar `app.ServidorApi` como *Java Application*.
- Linha de comando: `scripts\api.bat` ou `./scripts/api.sh`.

Sem as três variáveis de banco, a API sobe **em memória** (dados somem ao reiniciar), já com um
organizador (`organizador@demo.com` / `Senha123`) e um evento com 2 atividades — útil para testar
rápido sem depender do Supabase. Com as variáveis definidas, ela usa o Postgres de verdade.

A API sobe em `http://127.0.0.1:8080` (não use `localhost` — em algumas redes a resolução de
`localhost` engasga; `127.0.0.1` é direto).

**Passo 4 — abrir o site:** duplo clique em `site/index.html`. Ele consome a API em
`http://127.0.0.1:8080` (constante `API_BASE` no topo do `<script>`).

**Testes:** `scripts\testar.bat` / `./scripts/testar.sh` (mesmo de S2/S3).
- Sem banco: 106 testes (11 deles sobem a própria API numa porta livre e falam HTTP de verdade
  com ela — `ServidorApiTest` — sem depender de `curl`).
- Com banco (as 3 variáveis definidas, apontando para um Postgres com a migração da seção 0 já
  aplicada): 116 testes, incluindo persistência real e um bug que só aparecia com banco de
  verdade (ver seção 3).



## 2. O que foi construído

- **API REST sem framework** (`app.ServidorApi`), usando só `com.sun.net.httpserver.HttpServer`
  (parte do JDK) e um pequeno `Json` escrito à mão. Decisão registrada em D-02 (comparação com
  usar Gson/Jackson: mais prático num projeto real, mas adiciona uma dependência para uma API de
  7 rotas — trocar depois é mudar só a classe `Json`, o resto do sistema usa `Map`/`List`).
- **Rotas:**

  | Método | Rota | RF | Autenticação |
  |---|---|---|---|
  | POST | /api/usuarios | RF-01 cadastro | não |
  | POST | /api/sessoes | RF-02 login (devolve token) | não |
  | GET | /api/eventos | RF-10 eventos publicados | não |
  | GET | /api/eventos/{id}/atividades | RF-09 programação | não |
  | POST | /api/eventos | RF-04 criar evento | sim (organizador/admin) |
  | POST | /api/eventos/{id}/atividades | RF-05 criar atividade | sim (dono do evento/admin) |
  | POST | /api/inscricoes | RF-12 inscrição | sim (qualquer logado) |

- **Autorização no servidor (RNF-06):** token Bearer emitido no login (`TokenStore`, em
  memória); rota protegida sem token → 401; com token mas sem permissão (ex. participante
  criando evento) → 403, decidido pelo próprio caso de uso (`Usuario.exigirPermissaoParaGerenciarEventos`,
  `Evento.exigirGerenciadoPor`), não pela rota.
- **DTOs de saída** (`EventoPublicoDTO`, `AtividadeDTO`): o site nunca recebe o `organizadorId`
  nem outros dados internos — só o que ele precisa mostrar.
- **Site público** (`site/index.html`): HTML simples + `fetch()`, sem build, sem framework.
  Lista eventos publicados, mostra a programação de um evento, permite cadastro, login e
  inscrição — mesma API que qualquer outro cliente (desktop, quando existir) vai usar (RNF-02).
- **Testes HTTP automatizados** (`ServidorApiTest`): sobem o `HttpServer` de verdade numa porta
  livre, dentro da própria JVM de teste, e usam `java.net.http.HttpClient` — sem depender de
  `curl` nem de processos em segundo plano. Cobrem: cadastro + e-mail duplicado, login errado,
  criar evento sem token (401), participante tentando criar evento (403), organizador criando
  evento e atividade (201), inscrição respeitando limite de vagas, rota inexistente (404), corpo
  JSON inválido (400 em vez de derrubar o servidor).

## 3. O que foi corrigido nesta rodada (além do schema)

- **`EventoRepositoryDatabase`, `AtividadeRepositoryDatabase`, `UsuarioRepositoryDatabase`,
  `InscricaoRepositoryDatabase` (novo)** — reescritos contra o schema real confirmado + a
  migração da seção 0. Sem conversão de fuso horário no SQL: as colunas são
  `timestamp without time zone`, então a plataforma assume um único fuso para todos os eventos
  (`Evento.FUSO_PADRAO`, America/Sao_Paulo) em vez de guardar fuso por evento — mais simples e
  suficiente para o núcleo obrigatório.
- **`Atividade.local` agora é texto livre (String)**, não mais uma referência a uma tabela de
  locais que não existe no banco real (era `localId UUID`).
- **Bug real encontrado pelo teste de integração com banco de verdade:**
  `RealizarInscricaoUseCase` incrementava o contador de vagas da atividade só na memória e nunca
  chamava `atividadeRepository.salvar(atividade)`. Com o repositório em memória isso "funcionava
  por acidente" (é a mesma instância em RAM); com Postgres de verdade, a vaga nunca ficava
  ocupada de fato. Só apareceu quando o teste de integração passou a rodar contra um banco real —
  é o argumento vivo de por que RNF-12 pede teste automatizado, não só manual.

## 4. Pendências e próxima meta

- Sessão só em memória: reinício do servidor derruba todos os logins. Aceitável para o núcleo
  obrigatório; se quiserem algo mais robusto depois, é decisão de equipe (ex. token com validade).
- Sem edição/encerramento de evento nem cancelamento de inscrição pela API ainda (RF-04 parcial,
  RF-15 pendente) — entra em S5 junto com agenda e conflitos.
- Sem tela de desktop ainda — o "cadastro não existia" de antes está resolvido pelo site; o
  desktop, se a equipe decidir ter um, também deve falar só com esta API (D-01).

**Próxima meta (S5):** políticas de inscrição e agenda pessoal — ao menos um ponto de variação
resolvido por composição/polimorfismo (RN-07 conflito de horário já existe no domínio desde S2;
falta expor pela API e pelo site).
