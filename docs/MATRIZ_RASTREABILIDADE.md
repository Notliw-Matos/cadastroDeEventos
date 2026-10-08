# Matriz de rastreabilidade RF × ROO

Exigida pela seção 11.3 da especificação. Status: M = obrigatório, D = desejável.

| Requisito | Prio | ROO | Implementação | Teste / Demonstração | Status |
|---|---|---|---|---|---|
| RF-01 Cadastro | M | ROO-03,11 | `CadastrarUsuarioUseCase`, `Email`, `Senha`, `UsuarioRepositoryDatabase` | `CadastrarUsuarioUseCaseTest`, `PersistenciaIntegracaoTest` | ✔ Pronto |
| RF-02 Autenticação | M | ROO-07,09 | `AutenticarUsuarioUseCase`, `PasswordHasher`, `BCryptPasswordHasher` | `AutenticarUsuarioUseCaseTest`, `BCryptPasswordHasherTest` | ✔ Pronto |
| RF-03 Atualizar dados | M | ROO-02 | `Usuario` (campos, sem setters públicos) | — (CA-02 via demo) | ✔ Parcial¹ |
| RF-04 Criar/editar evento | M | ROO-01,02 | `Evento`, `CriarEventoUseCase`, `EventoRepositoryDatabase` | `EventoTest`, `CriarEventoUseCaseTest`, CA-01 | ✔ Pronto |
| RF-05 Tipos de atividade | M | ROO-01,05 | `Atividade.TipoAtividade` (enum), `CriarAtividadeUseCase` | `AtividadeTest`, `CriarAtividadeUseCaseTest` | ✔ Pronto |
| RF-06 Trilhas/categorias | M | ROO-01 | `Atividade.trilhaCategoria` (campo) | `PersistenciaIntegracaoTest` | ✔ Pronto |
| RF-07 Conflito de local/horário | M | ROO-03 | `IntervaloTempo.temConflitoCom()` | `IntervaloTempoTest` | ✔ Pronto |
| RF-08 Pessoas vinculadas | M | — | Não implementado | — | ✗ Pendente² |
| RF-09 Filtros de programação | M | ROO-01 | `GET /api/eventos/{id}/atividades`, `ListarAtividadesDoEventoUseCase` | `ServidorApiTest.listaAtividadesDoEventoPublico` | ✔ Pronto |
| RF-10 Site público | M | ROO-09 | `site/index.html`, `GET /api/eventos`, `ListarEventosPublicosUseCase` | `ServidorApiTest.eventosPublicadosApareceMasRascunhoNao` | ✔ Pronto |
| RF-11 Programação no site | M | ROO-09 | `site/index.html`, `verProgramacao()` | CA-01 (DemoFinal) | ✔ Pronto |
| RF-12 Inscrição pelo site | M | ROO-02,04 | `POST /api/inscricoes`, `RealizarInscricaoUseCase` | `RealizarInscricaoUseCaseTest`, `ServidorApiTest`, CA-02 | ✔ Pronto |
| RF-13 Regra de inscrição | M | ROO-04,05 | `PoliticaConflitoHorario` (Strategy) | `RealizarInscricaoUseCaseTest.comPoliticaDeAlerta...` | ✔ Pronto |
| RF-14 Controle de vagas | M | ROO-02,03 | `Vagas`, `Atividade.incrementarInscrito()` | `VagasTest`, `AtividadeTest.invarianteDeVagasNuncaEUltrapassada` | ✔ Pronto |
| RF-15 Cancelamento | M | ROO-02 | `Inscricao.cancelar()` | `RealizarInscricaoUseCaseTest.inscricaoCanceladaNaoGeraConflito` | ✔ Parcial³ |
| RF-16 Selecionar atividades | M | ROO-01 | `POST /api/inscricoes` + `GET /api/agenda` | `ServidorApiTest.agendaTrazAtividade...` | ✔ Pronto |
| RF-17 Agenda cronológica | M | ROO-01 | `VerAgendaUseCase` (ordena por início) | `ServidorApiTest.agendaTrazAtividade...`, CA-03 | ✔ Pronto |
| RF-18 Conflito de agenda | M | ROO-05 | `PoliticaConflitoBloqueante` / `PoliticaConflitoAlertaSomente` | `RealizarInscricaoUseCaseTest`, CA-03 | ✔ Pronto |
| RF-19 Critério de presença | M | ROO-05 | `CriterioFrequencia` (enum Strategy) | `RegistrarPresencaUseCaseTest` (3 critérios) | ✔ Pronto |
| RF-20 Gerar QR Code | M | ROO-07,10 | `GET /api/frequencia/meu-codigo` + `site/vendor/qrcode.js` | `ServidorApiTest.checkinComCodigoValido...`, CA-04 | ✔ Pronto |
| RF-21 Frequência por QR | M | ROO-09 | `POST /api/frequencia/checkin`, `RegistrarPresencaUseCase` | `ServidorApiTest.checkinComCodigoValido...` | ✔ Pronto |
| RF-22 Lançamento manual | M | ROO-02 | `POST /api/frequencia/manual` (tipo=MANUAL, operador registrado) | `ServidorApiTest.lancamentoManualRegistra...`, CA-05 | ✔ Pronto |
| RF-23 Situação de presença | M | ROO-05 | `GET /api/frequencia/{id}/situacao`, `calcularSituacaoDePresenca()` | `ServidorApiTest.checkinComCodigoValido...`, CA-04 | ✔ Pronto |
| RF-24 Criar questionário | M | ROO-01,02 | `POST /api/atividades/{id}/questionarios`, `CriarQuestionarioUseCase` | `ServidorApiTest.organizadorCriaQuestionario...`, CA-06 | ✔ Pronto |
| RF-25 Tipos de resposta | M | ROO-05 | `TipoPergunta` (enum com validarResposta), `Pergunta` | `AvaliacaoTest` (11 testes), CA-06 | ✔ Pronto |
| RF-26 Elegibilidade avaliação | M | ROO-02,11 | `ResponderQuestionarioUseCase` (RN-13) | `ResponderQuestionarioUseCaseTest`, CA-06 | ✔ Pronto |
| RF-27 Evitar duplicidade | M | ROO-02,11 | `RespostaQuestionarioRepository.buscarPorParticipanteEQuestionario` | `ResponderQuestionarioUseCaseTest.participanteNaoPodeResponderDuasVezes` | ✔ Pronto |
| RF-28 Consolidar resultados | M | ROO-01 | `ConsultarConsolidacaoUseCase`, `ConsolidacaoAvaliacaoDTO` | `ServidorApiTest`, CA-06 | ✔ Pronto |
| RF-29 Relatório inscrições | M | ROO-07,08 | `RelatorioInscricoes`, `GerarRelatorioUseCase` | `GerarRelatorioUseCaseTest`, `ServidorApiTest.relatorioInscricoesRetornaCsv`, CA-07 | ✔ Pronto |
| RF-30 Relatório frequência | M | ROO-07,08 | `RelatorioFrequencia`, `GerarRelatorioUseCase` | `GerarRelatorioUseCaseTest`, `ServidorApiTest.relatorioFrequenciaRetornaCsv`, CA-07 | ✔ Pronto |
| RF-31 Exportar relatório | M | ROO-07 | `RelatorioExportavel.toCsv()`, Content-Disposition header | `GerarRelatorioUseCaseTest.relatorioInscricoesContemParticipante` | ✔ Pronto |
| RF-32 Critério certificado | D | — | Não implementado | — | — |
| RF-33 Gerar certificado PDF | D | — | Não implementado | — | — |
| RF-34 Enviar certificado | D | — | Não implementado | — | — |
| RF-35 Certificado palestrante | D | — | Não implementado | — | — |
| RF-36 Espaço social | P | — | Não implementado | — | — |

---

**Notas:**

¹ RF-03 (atualizar dados do participante): o domínio suporta (campos encapsulados), mas não há
rota `PATCH /api/usuarios/{id}` — atualizar nome ou senha pela API ainda não está exposto.

² RF-08 (vincular pessoas às atividades com papéis): não implementado. `Atividade.getTitulo()`
e o tipo cobrem o mínimo, mas não há entidade `VinculoPessoa` com papel de palestrante/apresentador.

³ RF-15 (cancelamento de inscrição): `Inscricao.cancelar()` existe e é testado, mas não há rota
`DELETE /api/inscricoes/{id}` — o cancelamento não está exposto pela API.

---

## Cenários de aceitação CA-01 a CA-07

| Cenário | Status | Como demonstrar |
|---|---|---|
| CA-01 Publicação | ✔ | `scripts/demo-final` (CA-01) ou API: criar evento → publicar → listar |
| CA-02 Inscrição | ✔ | `scripts/demo-final` (CA-02) ou site: cadastrar → login → inscrever |
| CA-03 Agenda | ✔ | `scripts/demo-final` (CA-03) ou site: login → "Ver minha agenda" |
| CA-04 Frequência QR | ✔ | `scripts/demo-final` (CA-04) ou site: gerar QR → checkin → consultar situação |
| CA-05 Alternativa manual | ✔ | `scripts/demo-final` (CA-05) ou API: `POST /api/frequencia/manual` |
| CA-06 Avaliação | ✔ | `scripts/demo-final` (CA-06) ou API: criar questionário → responder → consolidação |
| CA-07 Relatório | ✔ | `scripts/demo-final` (CA-07) ou API: `GET /api/atividades/{id}/relatorio/inscricoes` |
