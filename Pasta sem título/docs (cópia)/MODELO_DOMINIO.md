# Modelo de domínio e arquitetura

## Diagrama arquitetural (Ports & Adapters / Hexagonal)

```mermaid
graph TD
    subgraph Adaptadores_de_entrada
        API["API HTTP\nServidorApi\napp/"]
        SITE["Site Público\nsite/index.html"]
        DEMO["DemoFinal\napp/DemoFinal.java"]
    end

    subgraph Aplicacao
        CU1["CadastrarUsuario\nAutenticarUsuario"]
        CU2["CriarEvento\nCriarAtividade"]
        CU3["RealizarInscricao\nVerAgenda"]
        CU4["RegistrarPresenca\nCalcularSituacao"]
        CU5["CriarQuestionario\nResponderQuestionario\nConsultarConsolidacao"]
        CU6["GerarRelatorio"]
    end

    subgraph Dominio
        E["Evento\n(ROO-01, ROO-02)"]
        A["Atividade\n(ROO-02, ROO-03)"]
        U["Usuario\n(ROO-01)"]
        I["Inscricao\n(ROO-01)"]
        RP["RegistroPresenca\n(ROO-03)"]
        Q["Questionario\nPergunta\nResposta\n(ROO-01,02,05)"]
        ST["Strategies:\nPoliticaConflito\nCriterioFrequencia\nTipoPergunta\n(ROO-05, ROO-10)"]
        VO["Objetos de valor:\nEmail, Senha\nIntervaloTempo, Vagas\n(ROO-03)"]
    end

    subgraph Portas_de_saida
        PR1["UsuarioRepository"]
        PR2["EventoRepository"]
        PR3["AtividadeRepository"]
        PR4["InscricaoRepository"]
        PR5["RegistroFrequenciaRepository"]
        PR6["QuestionarioRepository\nRespostaQuestionarioRepository"]
        PH["PasswordHasher"]
    end

    subgraph Adaptadores_de_saida
        DB["Postgres\npersistence/"]
        MEM["Em Memória\nmemoria/"]
        BCRYPT["BCryptPasswordHasher\nsecurity/"]
    end

    API --> CU1 & CU2 & CU3 & CU4 & CU5 & CU6
    SITE --> API
    DEMO --> CU1 & CU2 & CU3 & CU4 & CU5 & CU6

    CU1 & CU2 & CU3 & CU4 & CU5 & CU6 --> E & A & U & I & RP & Q & ST & VO
    CU1 & CU2 & CU3 & CU4 & CU5 & CU6 --> PR1 & PR2 & PR3 & PR4 & PR5 & PR6 & PH

    PR1 & PR2 & PR3 & PR4 & PR5 & PR6 --> DB
    PR1 & PR2 & PR3 & PR4 & PR5 & PR6 --> MEM
    PH --> BCRYPT
```

## Mapa de responsabilidades (ROO-01 a ROO-12)

| ROO | Evidência no código |
|---|---|
| ROO-01 Modelo de domínio | `Evento`, `Atividade`, `Inscricao`, `Usuario`, `RegistroPresenca`, `Questionario`, `Pergunta` — cada um com comportamento, não só dados |
| ROO-02 Encapsulamento | `Evento.publicar()`, `Evento.encerrar()`, `Atividade.incrementarInscrito()` — estado muda só pelos métodos; sem setters públicos |
| ROO-03 Objetos de valor | `Email`, `Senha`, `IntervaloTempo`, `Vagas`, `Pergunta`, `Resposta` — imutáveis, validam na construção |
| ROO-04 Composição | `RealizarInscricaoUseCase` composto com `PoliticaConflitoHorario`; `CriterioFrequencia` composto com `ValidadorFrequenciaStrategy` |
| ROO-05 Polimorfismo | `PoliticaConflitoHorario` (2 implementações), `CriterioFrequencia` (3 estratégias embutidas no enum), `TipoPergunta` (3 validações sem if/switch) |
| ROO-06 Herança | Não usada — composição foi sempre a opção mais simples para os pontos de variação existentes |
| ROO-07 Interfaces | `RelatorioExportavel`, `PoliticaConflitoHorario`, todas as portas de saída (`*Repository`, `PasswordHasher`) |
| ROO-08 SOLID | Open/Closed em `RelatorioExportavel` (novo formato = nova classe); Dependency Inversion em todos os casos de uso (dependem de interfaces, não de classes concretas) |
| ROO-09 Arquitetura hexagonal | `domain/` e `application/` não importam nada de `infrastructure/`; adaptadores injetados em `app/ServidorApi.criar()` |
| ROO-10 Padrões | Strategy (`PoliticaConflito`, `CriterioFrequencia`, `TipoPergunta`), Repository, Factory Method (`RespostaQuestionario.criar`) — ver D-07 |
| ROO-11 Erros do domínio | Hierarquia `DominioException` com 9 subclasses com mensagens legíveis; nunca `RuntimeException` genérica para regras de negócio |
| ROO-12 Testes e refatoração | 155 testes (JUnit 5), 4 refatorações documentadas em D-08, testes de mutação executados em S4/S6 para confirmar que os testes têm dentes |

## Diagrama de classes simplificado

```mermaid
classDiagram
    class Evento {
        -UUID id
        -UUID organizadorId
        -Estado estado
        +publicar()
        +encerrar()
        +exigirGerenciadoPor(Usuario)
    }
    class Atividade {
        -UUID eventoId
        -Vagas vagas
        -CriterioFrequencia criterio
        +incrementarInscrito()
        +conflitaCom(Atividade) bool
    }
    class Inscricao {
        -UUID participanteId
        -UUID atividadeId
        -Situacao situacao
        +cancelar()
        +isAtiva() bool
    }
    class RegistroPresenca {
        -UUID inscricaoId
        -TipoMarcacao tipo
        -UUID operadorId
    }
    class Questionario {
        -UUID atividadeId
        -List~Pergunta~ perguntas
    }
    class Pergunta {
        -TipoPergunta tipo
        +validarResposta(String)
    }
    class RespostaQuestionario {
        +criar(Questionario, Map) RespostaQuestionario$
    }

    Evento "1" --> "*" Atividade : contém
    Atividade "1" --> "*" Inscricao : tem
    Inscricao "1" --> "*" RegistroPresenca : acumula
    Atividade "1" --> "*" Questionario : avaliada por
    Questionario "1" --> "*" Pergunta : compõe
    Questionario "1" --> "*" RespostaQuestionario : recebe
```
