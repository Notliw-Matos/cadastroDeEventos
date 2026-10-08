# Entrega S6 — Frequência configurável

Sem migração de banco desta vez: `registros_frequencia` já batia 100% com o domínio desde que o
schema foi confirmado (seção 0 de `docs/S4_ENTREGA.md`).

## 1. O que foi construído

- **`RegistroFrequenciaRepository`** (porta nova) + adaptador em memória e adaptador de banco
  (`RegistroFrequenciaRepositoryDatabase`). Antes (S2), `RegistrarPresencaUseCase` guardava os
  registros numa `ArrayList` dentro de si mesmo — não sobrevivia a reinício do servidor nem
  passava pela API. Agora é uma porta de verdade, como todas as outras.
- **`RegistrarPresencaUseCase` reescrito**: resolve sozinho o `CriterioFrequencia` da atividade
  (o chamador não precisa mais passar a estratégia manualmente — RF-19/ROO-05) e verifica
  autorização do mesmo jeito que `CriarAtividadeUseCase` (RN-18: só quem gerencia o evento, ou
  administrador, registra presença nele).
- **QR Code de verdade** (RF-20, RF-21): `GET /api/frequencia/meu-codigo` gera um código opaco
  (UUID aleatório, sem senha nem dado pessoal — RN-10) associado ao participante logado. O site
  desenha esse código como um QR Code **real e escaneável**, usando a biblioteca
  `qrcode-generator` (kazuhikoarase, MIT) vendorizada em `site/vendor/qrcode.js` — é puro
  JavaScript, sem dependência de rede, e testei que ela realmente gera SVG válido antes de
  confiar nela (ver seção 3). O organizador "lê" o código (digita/cola o que a pessoa mostrou) e
  chama `POST /api/frequencia/checkin`.
- **Lançamento manual** (RF-22): `POST /api/frequencia/manual` — o organizador registra presença
  direto pelo `participanteId`, sem precisar de QR. Mesma autorização, mesmo caso de uso por
  baixo, só muda como a inscrição é encontrada.
- **Situação calculada** (RF-23): `GET /api/frequencia/{inscricaoId}/situacao` devolve
  `{presente: true/false}`, calculado a partir dos registros persistidos + o critério da
  atividade (Strategy já existente desde S2: check-in único, entrada/saída, ou manual).
- **Site**: seção "Meu código de presença" (gera e desenha o QR), "Registrar presença" (via QR
  ou manual) e "Consultar situação" — tudo com campos de texto simples, sem CSS, consistente com
  o resto do site.

## 2. Decisão de POO: onde fica a "leitura" do QR

RF-21 fala em "registrar frequência por leitura do QR Code". Duas formas foram consideradas:

1. **Câmera decodificando no navegador** (biblioteca JS tipo `jsQR` + `getUserMedia`) — mais
   realista visualmente, mas acrescenta uma segunda biblioteca, pede permissão de câmera e não
   muda nada na parte que importa para a disciplina (a persistência e a regra de negócio). Mais
   complexidade sem ganho de avaliação.
2. **Organizador informa o código que leu** (escolhida) — o código É gerado e desenhado como
   QR de verdade (não é só um texto fingindo ser QR); só a etapa de decodificação da câmera foi
   simplificada para "digitar/colar o que a pessoa mostrou". A regra de negócio (RF-20, RF-21,
   RN-10, autorização) está inteira; só a UX de apontar a câmera ficou de fora.

Fica registrado como extensão possível (não como pendência obrigatória): plugar `jsQR` no
`site/index.html` para decodificar via câmera é só trocar a função `registrarCheckin()` por uma
que lê da câmera e preenche o campo de código sozinha — o backend não muda em nada.

## 3. Evidência de que o QR Code vendorizado funciona de verdade

Antes de confiar na biblioteca, rodei isto fora do navegador (Node) e confirmei que ela gera um
SVG real:

```
$ node -e "... qr.addData('EVENTOS-QR:teste'); qr.make(); console.log(qr.createSvgTag(4).length)"
tamanho do SVG: 11452
<svg version="1.1" ...>
modulos: 29
```

## 4. Caminho válido, erro relevante, teste (protocolo da seção 9.1)

- **Caminho válido:** `checkinComCodigoValidoRegistraPresencaEAtualizaSituacao` (HTTP de ponta a
  ponta: gera código → organizador faz check-in → situação calculada vira `true`) e
  `registroDeFrequenciaPersisteComDataHoraRealEAutorizacao` (mesma coisa, mas contra Postgres de
  verdade, confirmando que a data/hora lida do banco é a real, não "agora" do objeto).
- **Erro relevante:** `checkinComCodigoInvalidoFalha` (código que não existe) e
  `outroOrganizadorNaoRegistraPresencaEmEventoAlheio` / `lancamentoManualSemAutorizacaoFalha`
  (RN-18).
- **Teste automatizado:** 9 testes novos de domínio/caso de uso + 7 HTTP + 1 de integração com
  banco real. Suíte: **131 com banco / 120 sem banco** (11 de integração ficam de fora sem
  `DB_PASSWORD`).
- **Mutação de verificação** (não fica no código, só documentado aqui): removi a linha
  `evento.exigirGerenciadoPor(operador)` manualmente e rodei a suíte — 3 testes quebraram, então
  a checagem está sendo testada de verdade, não só presente no código.

## 5. Bug encontrado nesta rodada

`RegistroPresenca` só tinha um construtor que sempre usava `LocalDateTime.now()` como data/hora —
inclusive ao RECONSTITUIR um registro lido do banco, o que faria qualquer registro antigo
aparecer como se tivesse acabado de acontecer. Corrigido com um segundo construtor que aceita a
data/hora explícita, usado só pelo adaptador de banco ao ler (ver `RegistroPresenca.java`). Pego
pelo teste de integração `registroDeFrequenciaPersisteComDataHoraRealEAutorizacao`, que verifica
que a data/hora lida é posterior ao instante em que o teste começou — não só "não nula".

## 6. Pendências e próxima meta

- Código de QR sem expiração nem limite: cada chamada a `/api/frequencia/meu-codigo` gera um
  token novo, mas o `TokenStore` nunca remove os antigos — na prática, qualquer código já gerado
  continua válido para sempre (até o servidor reiniciar, já que é tudo em memória). Funciona para
  o núcleo obrigatório, mas "validade" de verdade (expirar sozinho depois de alguns minutos) fica
  registrado como pendência de D-05.
- RF-28 (consolidar resultados de avaliação) e RF-29/30 (relatórios) continuam pendentes —
  natural para S7.
- Leitura de QR por câmera (ver seção 2) é só uma extensão possível, não uma pendência.

**Próxima meta (S7):** avaliações, relatórios e uma refatoração documentada com comparação
antes/depois.
