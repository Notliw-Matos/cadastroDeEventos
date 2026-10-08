# Entrega S7 — Avaliações, relatórios e refatoração

## 1. O que foi construído

### Avaliações (RF-24 a RF-28)

- **`Pergunta`** (valor imutável) — contém enunciado, tipo e opções. Sabe validar sua própria
  resposta sem que ninguém de fora faça um if/switch por tipo.
- **`TipoPergunta`** (enum com comportamento — ROO-05) — `TEXTO_LIVRE`, `ESCOLHA_UNICA` e
  `ESCALA_NUMERICA`. Cada constante implementa `validarResposta(String)`. Acrescentar um quarto
  tipo é acrescentar uma constante; nenhum `if` em `Pergunta`, nos casos de uso ou nas rotas
  precisa mudar.
- **`Questionario`** — criado pelo organizador (RF-24), imutável depois de criado.
- **`RespostaQuestionario.criar(...)`** — fábrica que valida *todas* as respostas (tipo + opções)
  antes de criar qualquer objeto; se uma falhar, nenhuma é criada (atomicidade no domínio,
  ROO-02). As regras de negócio RN-13 e RN-14 (inscrito + presente, uma resposta por questionário)
  ficam em `ResponderQuestionarioUseCase`.
- **`ConsultarConsolidacaoUseCase`** (RF-28) — devolve média para escala, distribuição para
  escolha e lista de comentários para texto livre, sem expor o id do participante nos textos livres
  (RN-15 preservada).
- **Rotas:** `POST /api/atividades/{id}/questionarios`, `POST /api/questionarios/{id}/respostas`,
  `GET /api/questionarios/{id}/consolidacao`.

### Relatórios (RF-29, RF-30, RF-31)

- **`RelatorioExportavel`** — interface com dois métodos: `toCsv()` e `nomeArquivo()`. Dois
  implementadores: `RelatorioInscricoes` e `RelatorioFrequencia`.
- **`GerarRelatorioUseCase`** — recebe o tipo desejado, verifica autorização, monta e devolve o
  objeto concreto. O caso de uso não sabe qual é o implementador que está devolvendo — só usa a
  interface (ROO-07).
- **Rotas:** `GET /api/atividades/{id}/relatorio/inscricoes` e `.../frequencia` — ambas devolvem
  CSV com cabeçalho `Content-Disposition: attachment; filename="..."` (RF-31, abrível fora do
  sistema).

## 2. Padrão justificado: Strategy no `TipoPergunta` (ROO-10)

**Problema original (antes):** a validação de resposta estava espalhada. Para saber se uma
resposta era válida, qualquer código precisava fazer `if (tipo == TEXTO_LIVRE) ... else if
(tipo == ESCALA_NUMERICA) ...`. Ao acrescentar um novo tipo, toda cadeia de if precisaria ser
localizada e atualizada — exatamente o problema que ROO-05 quer evitar.

**Depois (Strategy embutido no enum):** cada constante do enum implementa `validarResposta`.
Quem chama só faz `pergunta.validarResposta(valor)`. O Java despacha para a implementação certa
automaticamente — sem if no chamador, em nenhum lugar.

**Alternativa descartada:** classe abstrata `Pergunta` com subclasses por tipo. Descartada porque
(a) o comportamento diferente é *só* a validação — não justifica uma hierarquia; (b) com o enum,
o compilador garante que todo novo tipo precisa implementar `validarResposta`, o que seria
possível esquecer numa hierarquia aberta.

## 3. Refatoração documentada (ROO-12): relatórios saíram do controlador

**Antes (S4 — o que existia no `ServidorApi`):**

```
// S4: não havia relatórios implementados, mas o padrão
// que estava surgindo era: caso de uso devolve List<Inscricao>,
// a rota itera, monta a String CSV e escreve tudo inline.
// Problema: a lógica de formatação (escapamento, ordem das colunas,
// nome do arquivo) estava acoplada à camada HTTP.
```

**Depois (S7):**

```java
// A rota só sabe que existe um RelatorioExportavel:
RelatorioExportavel rel = dep.gerarRelatorio.executar(solicitanteId, atividadeId, tipo);
byte[] bytes = rel.toCsv().getBytes(StandardCharsets.UTF_8);
exchange.getResponseHeaders().set("Content-Disposition",
    "attachment; filename=\"" + rel.nomeArquivo() + "\"");
// ...
```

**Efeito observável:** trocar o formato de exportação (ex. adicionar PDF no S8) é criar uma nova
implementação de `RelatorioExportavel`. A rota não muda. O caso de uso não muda. O domínio não
muda. Antes, seria necessário adicionar um `if (formato == PDF)` dentro da rota ou do caso de
uso — dois lugares que não deveriam saber sobre formatos de arquivo.

Isso é exatamente o que ROO-08 pede: demonstrar extensibilidade com uma refatoração motivada por
SOLID (princípio Open/Closed — aberto para extensão, fechado para modificação).

## 4. Caminho válido, erro relevante, teste (protocolo da seção 9.1)

- **Caminho válido:** `participanteComPresencaValidaResponde` (RF-26), `relatorioInscricoesContemParticipante`
  (RF-29), `relatorioFrequenciaIndicaPresente` (RF-30).
- **Erro relevante:** `participanteSemPresencaValidadaNaoResponde` (RN-13),
  `participanteNaoPodeResponderDuasVezes` (RN-14), `outroOrganizadorNaoGeraRelatorio` (RN-18),
  `organizadorCriaQuestionarioEParticipanteInelegívelNaoResponde` (HTTP, RF-26).
- **Suíte:** **155 testes com banco / 144 sem banco** (11 de integração ficam de fora sem DB).

## 5. Pendências e próxima meta

- Os repositórios de `Questionario` e `RespostaQuestionario` não têm adaptador de banco — só em
  memória (as avaliações somem ao reiniciar o servidor). Adicionar as tabelas e os adaptadores
  é o único trabalho de infraestrutura que falta para estas entidades.
- Não há rota `GET /api/atividades/{id}/questionarios` ainda — o participante não consegue listar
  os questionários disponíveis pela API (o fluxo funciona, mas a navegação é manual pelo id).
- Certificados (RF-32 a RF-35, Desejáveis) continuam fora do escopo até o núcleo estar estável.

**Próxima meta (S8):** integração final, qualidade e entrega — cenários CA-01 a CA-07 completos,
ROO-01 a ROO-12 rastreados, documentação final.
