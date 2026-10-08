# Endpoints da API

Base URL: `http://127.0.0.1:8080` (ou `http://127.0.0.1:<porta>` conforme `API_PORT`).

Todas as rotas retornam `application/json`. Rotas marcadas com 🔒 exigem
`Authorization: Bearer <token>` obtido em `POST /api/sessoes`.

---

## Usuários e sessão

### `POST /api/usuarios` — Cadastro (RF-01)
```json
{ "nome": "Ana Silva", "email": "ana@exemplo.com", "senha": "Senha123" }
```
**201** → `{ "id", "nome", "email", "perfil": "PARTICIPANTE" }`  
**400** → e-mail já existe, e-mail inválido, senha fraca

### `POST /api/sessoes` — Login (RF-02)
```json
{ "email": "ana@exemplo.com", "senha": "Senha123" }
```
**200** → `{ "id", "nome", "email", "perfil", "token" }`  
**401** → credenciais inválidas

---

## Eventos

### `GET /api/eventos` — Listar eventos publicados (RF-10)
Sem autenticação. Retorna só eventos com estado `PUBLICADO`.  
**200** → `[ { "id", "titulo", "descricao", "inicio", "fim", "local" } ]`

### `POST /api/eventos` 🔒 — Criar evento (RF-04)
Apenas ORGANIZADOR ou ADMINISTRADOR.
```json
{ "titulo": "...", "descricao": "...", "inicio": "2026-11-01T08:00:00", "fim": "2026-11-01T18:00:00", "local": "..." }
```
**201** → `{ "id", "titulo", "descricao", "inicio", "fim", "local", "estado": "RASCUNHO" }`  
**401/403** → sem token ou perfil insuficiente

### `GET /api/eventos/{id}/atividades` — Programação (RF-09)
Sem autenticação. Retorna atividades em ordem cronológica.  
**200** → `[ { "id", "titulo", "tipo", "inicio", "fim", "temVaga" } ]`

### `POST /api/eventos/{id}/atividades` 🔒 — Criar atividade (RF-05)
Apenas o organizador dono do evento (ou ADMINISTRADOR).
```json
{ "titulo": "...", "tipo": "PALESTRA|OFICINA|APRESENTACAO_ORAL|POSTER|PRODUTO|MESA|OUTRO",
  "inicio": "...", "fim": "...", "vagas": 30 }
```
`vagas` omitido = sem limite. **201** → `{ "id", "titulo", "tipo", "inicio", "fim", "temVaga" }`

---

## Inscrições e agenda

### `POST /api/inscricoes` 🔒 — Inscrever-se (RF-12)
```json
{ "atividadeId": "uuid" }
```
**201** → `{ "id", "situacao": "CONFIRMADA", "avisos": [] }`  
`avisos` contém mensagens se houver conflito de horário e a política for "alerta" (S5).  
**400** → sem vagas, duplicada, conflito de horário (política padrão: bloqueante)

### `GET /api/agenda` 🔒 — Agenda pessoal (RF-16, RF-17)
**200** → `[ { "atividadeId", "titulo", "tipo", "inicio", "fim", "situacaoInscricao" } ]`  
Retorna só inscrições ativas, em ordem cronológica (RN-08).

---

## Frequência

### `GET /api/frequencia/meu-codigo` 🔒 — Gerar código de QR (RF-20)
**200** → `{ "codigo": "uuid-opaco" }` — sem senha nem dado pessoal (RN-10).

### `POST /api/frequencia/checkin` 🔒 — Registrar presença via QR (RF-21)
Apenas organizador do evento ou ADMINISTRADOR.
```json
{ "codigo": "uuid-do-qr", "atividadeId": "uuid", "tipo": "CHECK_IN|CHECK_OUT|MANUAL" }
```
**201** → `{ "id", "tipo", "dataHora" }`  
**400** → código inválido ou expirado

### `POST /api/frequencia/manual` 🔒 — Lançamento manual (RF-22)
Apenas organizador do evento ou ADMINISTRADOR.
```json
{ "participanteId": "uuid", "atividadeId": "uuid" }
```
**201** → `{ "id", "tipo": "MANUAL", "dataHora" }` — operador registrado (RN-11).

### `GET /api/frequencia/{inscricaoId}/situacao` 🔒 — Situação de presença (RF-23)
**200** → `{ "presente": true|false }` — calculado pelos registros + critério da atividade.

---

## Avaliações

### `POST /api/atividades/{id}/questionarios` 🔒 — Criar questionário (RF-24)
Apenas organizador do evento.
```json
{
  "titulo": "Avalie a atividade",
  "perguntas": [
    { "enunciado": "Nota de 1 a 10", "tipo": "ESCALA_NUMERICA" },
    { "enunciado": "Recomendaria?", "tipo": "ESCOLHA_UNICA", "opcoes": ["Sim","Nao","Talvez"] },
    { "enunciado": "Comentario livre", "tipo": "TEXTO_LIVRE" }
  ]
}
```
**201** → `{ "id", "titulo", "totalPerguntas" }`

### `POST /api/questionarios/{id}/respostas` 🔒 — Responder (RF-26)
Apenas participante inscrito E com presença validada (RN-13). Uma resposta por participante (RN-14).
```json
{ "respostas": { "<perguntaId>": "valor", "<perguntaId>": "8" } }
```
**201** → `{ "id", "questionarioId" }`  
**400** → inelegível, já respondeu, valor inválido para o tipo

### `GET /api/questionarios/{id}/consolidacao` 🔒 — Consolidação (RF-28)
Apenas organizador do evento.  
**200** → `{ "questionario", "totalRespostas", "perguntas": [ { "enunciado", "tipo", "media"|"distribuicao"|"comentarios" } ] }`

---

## Relatórios

### `GET /api/atividades/{id}/relatorio/inscricoes` 🔒 — Relatório de inscritos (RF-29, RF-31)
Apenas organizador do evento. Retorna CSV com `Content-Disposition: attachment; filename="relatorio_inscricoes.csv"`.  
Colunas: `atividade;participanteId;nome;email;situacao`

### `GET /api/atividades/{id}/relatorio/frequencia` 🔒 — Relatório de frequência (RF-30, RF-31)
Apenas organizador do evento. Retorna CSV com `Content-Disposition: attachment; filename="relatorio_frequencia.csv"`.  
Colunas: `atividade;participanteId;nome;totalMarcacoes;presente`

---

## Códigos de erro comuns

| HTTP | Significado |
|---|---|
| 400 | Regra de domínio violada (mensagem explicativa no campo `mensagem`) |
| 401 | Sem token ou token inválido |
| 403 | Token válido, mas sem permissão para esta operação |
| 404 | Recurso não encontrado ou rota inexistente |
| 500 | Erro interno (ver log do servidor) |
