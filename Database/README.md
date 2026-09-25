# Scripts SQL — Plataforma de Gestão de Eventos (Supabase / Postgres)

Alinhados à Especificação de Requisitos do Prof. Marcio (RF, RN, RNF, ROO-09).

## Onde rodar
No Supabase: **SQL Editor** (menu lateral) → cole o conteúdo de cada arquivo, nesta ordem, e clique em **Run**.
(Se o banco já tiver algo criado de uma tentativa anterior e der erro de "already exists", rode `00_reset.sql` primeiro.)

## Ordem de execução

| Arquivo | O que faz |
|---|---|
| `00_reset.sql` | (opcional) apaga tudo, pra recomeçar do zero |
| `01_schema.sql` | extensões, enums, todas as tabelas + RLS ligado (sem policy ainda) |
| `02_seed_usuarios.sql` | 2 administradores, 6 organizadores, **500 participantes** (RNF-10). Senha de todos: `Demo@123`, com hash via `pgcrypto` (RNF-05) |
| `03_seed_evento_locais.sql` | 1 evento publicado + 10 locais |
| `04_seed_atividades.sql` | **100 atividades** (RNF-10) em 3 dias, 10 locais, 8 trilhas |
| `05_seed_inscricoes_frequencia.sql` | inscreve participantes no evento e em atividades, e gera frequência de exemplo — **versão corrigida**, ver nota abaixo |
| `06_policies_leitura_publica.sql` | libera leitura pública de `atividades`, `locais` e `atividades_pessoas` (RF-10/RF-11) |
| `07_indices_performance.sql` | índices nas chaves estrangeiras que faltavam (RNF-10) |
| `08_regras_negocio.sql` | trava no banco as regras RN-06, RN-07, RN-11, RN-13, RN-15 (ver tabela abaixo) |

### ⚠️ Correção no `05_seed_inscricoes_frequencia.sql`
A primeira versão usava `cross join lateral (... order by random() limit N)`, mas como essa
subquery não referenciava o participante, o Postgres a avaliou **uma única vez** e reaproveitou
o mesmo resultado pra todo mundo — todos os 500 participantes caíram nas duas mesmas atividades.
A versão atual usa um laço (`do $$ ... $$`) participante por participante, o que corrige isso e
de quebra já respeita os gatilhos do `08_regras_negocio.sql` (se rodar `05` depois de `08`, ainda
melhor — inscrições que dariam conflito de horário ou estourariam vagas são puladas automaticamente).
**Se você já rodou a versão antiga, limpe antes com:**
```sql
delete from frequencias;
delete from inscricoes_atividade;
```
e rode o `05` (e depois o `08`, se ainda não tiver rodado) de novo.

## Regras de negócio (RN) — o que está travado no banco

| Regra | Onde | O que faz |
|---|---|---|
| RN-06 | trigger em `inscricoes_atividade` | bloqueia inscrição se as vagas da atividade já estiverem esgotadas |
| RN-07 | trigger em `inscricoes_atividade` | bloqueia inscrição se o participante já tem outra atividade confirmada no mesmo horário |
| RN-11 | `check` em `frequencias` | impede salvar frequência `MANUAL` sem um `registrado_por` |
| RN-13 | trigger em `respostas` | só deixa responder questionário quem tem frequência registrada naquela atividade |
| RN-15 | coluna `questionarios.anonimo` | o app usa esse campo pra avisar antes do envio se a resposta é anônima |

**Ficaram de fora do banco de propósito** (são decisão de código/UX no Java, não dá pra travar só com SQL):
- **RN-10** — QR Code não pode conter senha/dado sensível: depende de como você monta o payload do QR no backend.
- **RN-12** — rastreabilidade de correções manuais: recomendação é nunca fazer `UPDATE`/`DELETE` em `frequencias`, só inserir um novo registro; se quiser auditoria formal, dá pra criar uma tabela extra depois.
- **RN-16/RN-17** — elegibilidade de certificado e não alterar histórico silenciosamente: regra de processo calculada pela aplicação.
- **RN-18** — só admin/organizador altera dados do evento: autorização real precisa estar no backend Java (RNF-06); o RLS aqui é reforço, não a defesa principal.

## Depois de rodar

1. Vá em **Project Settings → API Keys** e copie:
   - **Project URL**
   - **Publishable key** (`sb_publishable_...`) → usar no site público / app desktop
   - **Secret key** (`sb_secret_...`) → usar SOMENTE no backend Java, nunca no cliente
2. Vá em **Project Settings → Database** para pegar a *connection string* (Session pooler, porta
   5432) e usar via JDBC no adaptador de persistência da aplicação Java.
3. Lembre-se do RNF-06: a checagem de autorização por perfil precisa acontecer na sua API/backend,
   não só via RLS ou via ocultar botões na interface.

## Observações de modelagem

- Não existe tabela de "papéis" separada para avaliador — avaliar é uma capacidade temporária do
  participante inscrito e com presença validada (RN-13), verificada por trigger.
- `Visitante` não tem registro em `usuarios`: ele acessa o site público sem autenticação (RF-10/RF-11).
- Este schema é um ponto de partida — a especificação deixa a modelagem de classes a cargo da
  equipe; adapte nomes/tabelas conforme o modelo de domínio que vocês desenharem.
