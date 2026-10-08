# Entregas S2 e S3 — Plataforma de Gestão de Eventos

Este documento serve de **README das entregas S2 e S3** e como roteiro para a demonstração de terça-feira.

## 1. Como executar

### Configuração do banco (uma vez)
A senha do banco **não fica mais no código**. Defina a variável de ambiente `DB_PASSWORD`.

| Sistema | Como definir |
|---|---|
| Windows (cmd, sessão atual) | `set DB_PASSWORD=sua_senha` |
| Windows (PowerShell) | `$env:DB_PASSWORD="sua_senha"` |
| Linux / macOS / Git Bash | `export DB_PASSWORD=sua_senha` |
| Eclipse | *Run → Run Configurations → (sua classe) → aba **Environment** → New: `DB_PASSWORD`* |

Opcionais: `DB_URL` e `DB_USER` (padrão: Session Pooler do Supabase do projeto).

### Rodar os testes
- **Eclipse:** clique direito em `src/test/java` → *Run As → JUnit Test*.
- **Linha de comando:** `scripts\testar.bat` (Windows) ou `./scripts/testar.sh`.
- Sem `DB_PASSWORD`: rodam os 96 testes unitários (sem banco, sem interface); os 10 de integração são ignorados.
- Com `DB_PASSWORD`: rodam os 106, incluindo persistência real. Os testes de integração apagam tudo que criam.

### Rodar a demonstração
- **Eclipse:** executar `app.DemoS2S3` como *Java Application* (com `DB_PASSWORD` na aba Environment).
- **Linha de comando:** `scripts\demo.bat` ou `./scripts/demo.sh`. Use `--manter` para não apagar os dados criados.

## 2. O que mudou e por quê

**Correções que impediam o projeto de compilar**
- `ConexaoDatabase` tinha marcadores de merge (`<<<<<<< HEAD`) — resolvido.
- Estratégias de frequência usavam constantes inexistentes (`CHECK_IN_UNICO`, `ENTRADA`, `SAIDA`) — alinhadas ao enum real.
- `RealizarInscricaoUseCase` chamava `listarPorParticipante` e `isAtiva`, que não existiam — implementados.

**S2 — Objetos, invariantes e persistência**
- **Repositórios alinhados ao schema real.** Antes o Java usava colunas que não existem (`data_hora_inicio`, `capacidade_maxima`, `local_ou_link`, `total_inscritos`) e nunca preenchia `eventos.organizador_id` (NOT NULL): salvar um evento no Supabase falharia.
- **Objetos de valor:** `Email` (normaliza), `Senha` (política, nunca impressa), `IntervaloTempo` (fim > início), `Vagas` (limitada/ilimitada).
- **Invariantes nos objetos:** `Evento` (RASCUNHO → PUBLICADO → ENCERRADO, encerrado não muda), `Atividade` (nunca passa do limite de vagas), sem setters públicos.
- **Erros do domínio explícitos (ROO-11):** hierarquia `DominioException` com mensagens para o usuário.
- **Fuso horário (RN-20):** o domínio usa horário local do evento; o SQL converte de/para `timestamptz` com o fuso do próprio evento.

**S3 — Casos de uso, portas e autenticação**
- **Portas de entrada** (`application.ports.in`): `CadastrarUsuario`, `AutenticarUsuario`, `CriarEvento`, `CriarAtividade`.
- **Portas de saída** (`domain.ports`): `UsuarioRepository`, `PasswordHasher` (novas), além das existentes.
- **Adaptadores:** `UsuarioRepositoryDatabase`, `BCryptPasswordHasher` (compatível com o `crypt(..., gen_salt('bf'))` do seed — os usuários de demonstração conseguem logar com `Demo@123`).
- **Autorização no servidor (RNF-06/RN-18):** administrador gerencia qualquer evento; organizador só os seus; participante nenhum.
- **Teste sem interface:** os casos de uso rodam com repositórios em memória (`src/test/java/support`).

## 3. Roteiro da demonstração (protocolo da seção 9.1)

1. **Meta e requisitos:** S2 — RF-04, RF-05, ROO-02/03; S3 — RF-01, RF-02, RNF-05/06, ROO-07/09.
2. **Executar:** `app.DemoS2S3` (cadastro, login, criar evento e atividade, releitura do banco).
3. **Caminho válido + erro relevante:** cadastro válido × e-mail duplicado; login × senha errada; organizador cria evento × participante é NEGADO.
4. **Teste relevante:** `CriarEventoUseCaseTest.participanteNaoCriaEventoENadaEPersistido` (S3) e `AtividadeTest.invarianteDeVagasNuncaEUltrapassada` (S2).
5. **Pendências / próxima meta:** ver seção 5.
6. **Decisão de POO da semana:**
   - **S2 — invariante protegida:** `Vagas` e `IntervaloTempo` validam na construção; `Atividade` só ganha inscrito por `incrementarInscrito()`, que respeita o limite. Não existe caminho para criar objeto inválido.
   - **S3 — acoplamento reduzido / inversão de dependência:** os casos de uso dependem de `UsuarioRepository` e `PasswordHasher` (interfaces do domínio). Trocar bcrypt por outro algoritmo, ou Postgres por memória, não toca em nenhuma regra.
7. **Refatoração (antes/depois):**
   - *Antes:* `RealizarInscricaoUseCase` calculava sobreposição de horários dentro do caso de uso. *Depois:* `Atividade.conflitaCom()` e `IntervaloTempo.temConflitoCom()` — a regra mora no objeto que a conhece (teste: `IntervaloTempoTest`).
   - *Antes:* `Atividade` guardava `capacidadeMaxima` (int) + contador, e o `total_inscritos` não era relido do banco (voltava sempre 0). *Depois:* `Vagas` (VO) e total derivado das inscrições confirmadas.

## 4. Decisões registradas (D-xx)

| ID | Decisão | Alternativa descartada |
|---|---|---|
| D-03 | JDBC puro + SQL explícito nos adaptadores; `AT TIME ZONE` para RN-20 | JPA/Hibernate: esconderia o mapeamento e puxaria anotações para o domínio |
| D-02 (parcial) | bcrypt (jBCrypt, licença ISC, `org/mindrot/BCrypt.java`) atrás da porta `PasswordHasher` | PBKDF2 da JDK: mais simples, mas incompatível com os hashes do seed |
| D-06 (parcial) | `CriterioFrequencia` (enum do banco) carrega sua `ValidadorFrequenciaStrategy` | `switch` por critério espalhado pelos casos de uso |
| — | Cadastro público sempre cria PARTICIPANTE | Receber o perfil do formulário (escalada de privilégio) |
| — | Total de inscritos é derivado, não coluna | Coluna `total_inscritos`: duas fontes de verdade que podem divergir |
| RN-06 | Vagas também travadas no banco (`08_regras_negocio.sql`) | Confiar só na aplicação (corrida entre duas inscrições simultâneas) |

## 5. Pendências e próxima meta

**Pendências conhecidas**
- Não há adaptador de banco para `Inscricao` (nem para `Frequencia`); `RegistrarPresencaUseCase` ainda guarda registros em memória. **S4/S6.**
- Descompasso a resolver em S6: o banco registra a *origem* da marcação (`QR_CODE`/`MANUAL`) e o domínio modela *CHECK_IN/CHECK_OUT/MANUAL*. Precisa de decisão de modelagem (origem × tipo).
- Sem sessão/token: o login devolve o usuário, mas a API (S4) precisa emitir e validar um token.
- Casos de uso ainda devolvem `Evento`/`Atividade` (entidades); DTOs de saída ficam para S4 (separação DTO/aplicação/domínio).
- Administrador ainda não cadastra organizadores (RF de gestão de usuários).
- `IntervaloTempo` agora exige fim **estritamente** posterior ao início (o banco aceita igualdade para eventos).

**Próxima meta (S4):** adaptador REST + site consumindo a API, inscrição integrada, DTOs.

## 6. Matriz de rastreabilidade (parcial S2/S3)

| Requisito | ROO | Implementação | Teste / demonstração | Status |
|---|---|---|---|---|
| RF-01, RN-01 | ROO-03, 11 | `CadastrarUsuarioUseCase`, `Email`, `Senha`, `UsuarioRepositoryDatabase` | `CadastrarUsuarioUseCaseTest`, `PersistenciaIntegracaoTest.bancoRejeitaEmailDuplicado…` | Pronto |
| RF-02, RNF-05 | ROO-07, 09 | `AutenticarUsuarioUseCase`, `PasswordHasher`, `BCryptPasswordHasher` | `AutenticarUsuarioUseCaseTest`, `BCryptPasswordHasherTest` | Pronto |
| RNF-06, RN-18 | ROO-02, 05 | `Usuario.Perfil`, `Evento.podeSerGerenciadoPor` | `EventoTest`, `CriarEventoUseCaseTest`, `CriarAtividadeUseCaseTest` | Pronto |
| RF-04, RN-04 | ROO-01, 02 | `Evento`, `EventoRepositoryDatabase` | `EventoTest`, `PersistenciaIntegracaoTest.eventoPersiste…` | Pronto (falta editar/encerrar via caso de uso) |
| RF-05, RN-06 | ROO-02, 03 | `Atividade`, `Vagas`, `AtividadeRepositoryDatabase` | `AtividadeTest`, `VagasTest`, integração | Pronto |
| RN-20 | ROO-03 | `AT TIME ZONE` nos repositórios | integração (horário volta idêntico) | Pronto |
| RF-19 | ROO-05, 10 | `CriterioFrequencia` + `ValidadorFrequenciaStrategy` (Strategy) | `RegistrarPresencaUseCaseTest` | Parcial (S6) |
| RNF-12 | ROO-12 | JUnit 5, adaptadores em memória | `scripts/testar` | Pronto |
