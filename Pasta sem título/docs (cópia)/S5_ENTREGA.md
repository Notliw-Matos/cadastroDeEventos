# Entrega S5 — Políticas de inscrição e agenda

## 1. O que foi construído

- **`PoliticaConflitoHorario`** (`domain.strategy`) — o ponto de variação real desta semana
  (RN-07: "conflitantes devem ser alertadas OU bloqueadas por uma política uniforme"). Interface
  com dois métodos de implementação:
  - `PoliticaConflitoBloqueante` — política padrão da plataforma: lança `ConflitoDeHorarioException`,
    a inscrição não acontece (RF-18 "impedir").
  - `PoliticaConflitoAlertaSomente` — deixa a inscrição acontecer mesmo com sobreposição, mas
    devolve um aviso explicando o conflito (RF-18 "alertar").

  `RealizarInscricaoUseCase` recebe a política pelo construtor (composição — ROO-04) e nunca
  pergunta qual delas está ativa (polimorfismo — ROO-05): ele só chama
  `politica.verificar(nova, existente, avisos)` e segue. Trocar de bloqueante para alerta é trocar
  um construtor, sem tocar no caso de uso. Ver `RealizarInscricaoUseCaseTest` para os dois
  comportamentos lado a lado.

- **Agenda pessoal (RF-16, RF-17, RN-08)** — `VerAgendaUseCase` junta as inscrições ativas do
  participante com os dados da atividade (via `AtividadeRepository`) e devolve em ordem
  cronológica. Inscrição cancelada não aparece (RN-08). Exposto em `GET /api/agenda`
  (autenticado) e no site, botão "Ver minha agenda".

- **`POST /api/inscricoes` agora devolve `avisos`** — um array (vazio na política padrão) com
  qualquer aviso de conflito. O site mostra esse aviso ao lado da confirmação.

## 2. Decisão de POO da semana

Duas rotas de fazer isto foram consideradas:
1. **If/else dentro do caso de uso** decidindo bloquear ou avisar a partir de uma flag booleana —
   mais simples de escrever, mas cada nova regra ("bloquear só para atividades obrigatórias",
   por exemplo) viraria mais um `if`. Foi descartada por violar o próprio ROO-05 ("evitar cadeias
   de if/switch por tipo").
2. **Strategy** (escolhida) — acrescentar uma terceira política no futuro é só escrever uma classe
   nova; `RealizarInscricaoUseCase` não muda. É a mesma ideia já usada para `CriterioFrequencia`
   em S2 — reaproveitar o padrão onde ele resolve o mesmo tipo de problema (duas ou mais regras
   concorrentes para o mesmo ponto de decisão) é mais defensável do que inventar um padrão
   diferente só para variar.

## 3. Caminho válido, erro relevante, teste (protocolo da seção 9.1)

- **Caminho válido:** `organizadorCriaEventoEAtividade` (S4) + `inscreveParticipante`
  continuam passando; `comPoliticaDeAlertaAmbasAsInscricoesSaoAceitasEGeraAviso` mostra as duas
  inscrições conflitantes sendo aceitas com aviso.
- **Erro relevante:** `naoInscreveEmAtividadeComHorarioConflitante` (política padrão) continua
  barrando a segunda inscrição.
- **Teste automatizado:** 5 testes novos (unitários) + 2 HTTP (`agendaSemLoginRetorna401`,
  `agendaTrazAtividadeInscritaEmOrdemCronologica`) — suíte agora com **121 testes com banco /
  111 sem banco** (10 de integração ficam de fora sem `DB_PASSWORD`).

## 4. Pendências e próxima meta

- A política de conflito é fixa em tempo de compilação (escolhida ao montar o servidor); não há
  endpoint para trocar em runtime. Para o núcleo obrigatório isso não é exigido — RN-07 só pede
  "uma política uniforme e documentada", o que está satisfeito.
- Cancelamento de inscrição (RF-15) ainda não tem caso de uso nem rota — fica para quando a
  equipe decidir encaixar.
- `local` de atividade e trilha/categoria (RF-06) ainda não aparecem no formulário de criação de
  atividade da API/site — dá para adicionar sem mexer no domínio, que já tem os dois campos.

**Próxima meta (S6):** frequência configurável — QR Code e lançamento manual, persistindo em
`registros_frequencia` (a tabela já existe no schema real, sem migração pendente desta vez).
