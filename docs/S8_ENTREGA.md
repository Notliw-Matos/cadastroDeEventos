# Entrega S8 — Integração, qualidade e entrega final

## 1. O que foi entregue nesta rodada

- **`DemoFinal.java`** (`app/`): executa os cenários CA-01 a CA-07 em sequência, em memória,
  sem banco, sem interface. Mostra caminho válido e erro relevante para cada cenário. Serve de
  roteiro para a apresentação final (seção 11.2 da especificação).
  Use: `scripts/demo-final.bat` (Windows) ou `./scripts/demo-final.sh`.

- **Documentação final completa** (`docs/`):
  - `README.md` — pré-requisitos, configuração, execução e usuários de demonstração (RNF-15).
  - `DECISOES_D01_D08.md` — D-01 a D-08 com problema, alternativa descartada e efeito.
  - `ENDPOINTS_API.md` — todas as rotas, payloads de entrada/saída e códigos de erro.
  - `MODELO_DOMINIO.md` — diagrama arquitetural Ports & Adapters, diagrama de classes e mapa
    de responsabilidades ROO-01 a ROO-12.
  - `MATRIZ_RASTREABILIDADE.md` — RF-01 a RF-36, ROO relacionado, implementação, teste e status.
  - `Database/migracao_s4.sql` — migration com `IF NOT EXISTS`, segura de rodar novamente.

## 2. Estado final da suíte de testes

| Modo | Testes encontrados | Passam | Ignorados |
|---|---|---|---|
| Sem banco | 155 | 144 | 11 (integração com DB) |
| Com banco (Postgres local ou Supabase) | 155 | 155 | 0 |

Rodar: `scripts/testar.bat` ou `./scripts/testar.sh`.  
Com banco: definir `DB_URL`, `DB_USER`, `DB_PASSWORD` antes.

## 3. Cenários CA-01 a CA-07 — roteiro de demonstração

Todos executáveis com `scripts/demo-final` em menos de 10 segundos, sem banco.
Para demonstrar com dados reais no banco, use a API + site.

| Cenário | RF/RN cobertos | Erro relevante demonstrado |
|---|---|---|
| CA-01 Publicação | RF-04,05, RN-04,18 | Participante tenta criar evento → 403 |
| CA-02 Inscrição | RF-12,13,14, RN-01,06 | E-mail duplicado + sem vaga |
| CA-03 Agenda | RF-16,17,18, RN-07,08 | Conflito de horário bloqueado |
| CA-04 Frequência QR | RF-20,21,23, RN-09,10 | CHECK_IN_UNICO vs ENTRADA_SAIDA |
| CA-05 Alternativa manual | RF-22, RN-11,12,18 | Outro organizador bloqueado (RN-18) |
| CA-06 Avaliação | RF-24,25,26,27,28, RN-13,14 | Sem presença + resposta duplicada |
| CA-07 Relatório | RF-29,30,31, RN-18 | Participante tenta gerar relatório → 403 |

## 4. ROO-01 a ROO-12 rastreados

Ver `docs/MODELO_DOMINIO.md` (mapa de responsabilidades) e `docs/MATRIZ_RASTREABILIDADE.md`.
Resumo das evidências mais fortes:

- **ROO-02 Encapsulamento:** `Atividade.incrementarInscrito()` — a única porta para ocupar
  uma vaga; `Evento.publicar()`/`encerrar()` — estado muda só pelos métodos.
- **ROO-03 Objetos de valor:** `IntervaloTempo` rejeita fim ≤ início na construção; `Vagas`
  nunca permite total acima do limite; `Email` normaliza caixa e valida formato.
- **ROO-05 Polimorfismo:** três pontos de variação reais sem `if/switch` — `PoliticaConflitoHorario`,
  `CriterioFrequencia`, `TipoPergunta`.
- **ROO-09 Arquitetura hexagonal:** `domain/` e `application/` não importam nada de
  `infrastructure/`. Trocar Postgres por outro banco = trocar os adaptadores, não tocar no domínio.
- **ROO-10 Padrões:** Strategy (três usos), Repository, Factory Method — cada um documentado
  em D-07 com o problema que resolve e a alternativa descartada.
- **ROO-12 Testes e refatoração:** 4 refatorações documentadas em D-08; testes de mutação
  executados em S4 e S6 confirmaram que os testes detectam falhas reais.

## 5. Limitações conhecidas

| Item | Situação |
|---|---|
| RF-08 Vincular pessoas às atividades (palestrante, apresentador) | Não implementado — campo `titulo` e tipo cobrem o mínimo para a programação |
| RF-03 Atualizar dados do participante | Domínio suporta; rota `PATCH /api/usuarios/{id}` não exposta |
| RF-15 Cancelamento de inscrição | `Inscricao.cancelar()` existe e é testado; rota `DELETE /api/inscricoes/{id}` não exposta |
| RF-32 a RF-35 Certificados (Desejável D) | Não implementados |
| RF-36 Rede social (Opcional P) | Não implementado |
| Repositórios de Questionario/RespostaQuestionario | Só em memória — avaliações não persistem após reinício |
| Expiração do código QR | Sem TTL — código válido até o servidor reiniciar |
| Sessão do usuário | Token em memória — reinício derruba todos os logins |

## 6. Como executar em outro computador (RNF-15)

1. Instalar JDK 11+.
2. `git clone https://github.com/Notliw-Matos/cadastroDeEventos.git`
3. `scripts\testar.bat` (Windows) ou `./scripts/testar.sh` — deve passar 144 testes sem banco.
4. Para a demonstração: `scripts\demo-final.bat` ou `./scripts/demo-final.sh`.
5. Para banco real: definir variáveis, rodar migração (`Database/migracao_s4.sql`) e seed.
6. `scripts\api.bat` + abrir `site/index.html`.
