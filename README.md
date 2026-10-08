# Plataforma de Gestão de Eventos — POO II

Projeto integrador de Programação Orientada a Objetos II (UEG). Sistema de gestão de eventos
acadêmicos e profissionais com cadastro, inscrição, frequência por QR Code, avaliações e
relatórios CSV.

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Java (JDK) | 11 |
| PostgreSQL | 14 (opcional — sem ele, a API roda em memória) |
| JUnit 5 Console Standalone | incluso em `tools/` |
| Driver PostgreSQL | incluso em `lib/` |

Não é necessário Maven, Gradle, npm nem nenhum build tool externo.

## Estrutura do projeto

```
src/main/java/
  domain/          — entidades, objetos de valor, estratégias, portas de saída
  application/     — casos de uso e DTOs
  infrastructure/  — adaptadores de banco (persistence/) e em memória (memoria/)
  app/             — composition root: ServidorApi (API HTTP) e DemoFinal (demo CA-01..07)
site/              — site público HTML+JS (abre direto no navegador)
scripts/           — atalhos para compilar, testar, rodar API, seed e demo final
docs/              — decisões de entrega (S2..S8)
lib/               — postgresql-42.7.13.jar
tools/             — junit-platform-console-standalone.jar
Database/          — scripts SQL de criação e migração do banco
```

## Como executar

### 1. Configurar a conexão com o banco (opcional)

Defina três variáveis de ambiente — **nenhuma senha vai para o código**:

| Variável | Onde encontrar |
|---|---|
| `DB_URL` | Supabase → Project Settings → Database → Session pooler → `jdbc:postgresql://HOST:5432/postgres` |
| `DB_USER` | Mesmo lugar (ex.: `postgres.jrqlzhxbpfodteflnypu`) |
| `DB_PASSWORD` | Senha do banco definida ao criar o projeto |

**Eclipse:** Run Configurations → aba Environment → New para cada variável.  
**Windows cmd:** `set DB_URL=...` `set DB_USER=...` `set DB_PASSWORD=...`  
**Linux/macOS:** `export DB_URL=... DB_USER=... DB_PASSWORD=...`

Sem as variáveis, a API sobe em **modo memória** (dados somem ao reiniciar), com dados de
demonstração já criados. Suficiente para testar sem mexer no Supabase.

### 2. Carregar dados no banco (só uma vez, quando usar o Supabase)

Execute a migração SQL em `Database/migracao_s4.sql` no SQL Editor do Supabase, depois:

```
scripts\seed.bat        (Windows)
./scripts/seed.sh       (Linux/macOS)
```

Cria 500 participantes, 1 evento publicado e 100 atividades (RNF-10).

### 3. Rodar a API

```
scripts\api.bat         (Windows)
./scripts/api.sh        (Linux/macOS)
```

API em `http://127.0.0.1:8080`. Use `127.0.0.1`, não `localhost`.

### 4. Abrir o site público

Duplo clique em `site/index.html`. Consome a API em `http://127.0.0.1:8080`.

### 5. Rodar os testes

```
scripts\testar.bat      (Windows)
./scripts/testar.sh     (Linux/macOS)
```

- **Sem banco:** 144 testes unitários e HTTP (11 de integração ficam de fora).
- **Com banco:** 155 testes no total.

### 6. Rodar a demonstração final (CA-01 a CA-07)

```
scripts\demo-final.bat  (Windows)
./scripts/demo-final.sh (Linux/macOS)
```

Não precisa de banco. Executa todos os cenários obrigatórios em memória e imprime
caminho válido + erros relevantes para cada um.

## Usuários de demonstração

Quando a API roda **em memória** (sem variáveis de banco), estes dados são criados automaticamente:

| E-mail | Senha | Perfil |
|---|---|---|
| `organizador@demo.com` | `Senha123` | ORGANIZADOR |

Para criar participantes, use o site (`Cadastrar`) ou `POST /api/usuarios`.

Quando usar o **banco com seed**, os usuários criados são:

| E-mail | Senha | Perfil |
|---|---|---|
| `organizador.seed@evento.com` | `Demo1234` | ORGANIZADOR |
| `participante1@seed-demo.com` … `participante500@seed-demo.com` | `Demo1234` | PARTICIPANTE |

## Migração de banco (primeira vez no Supabase)

O schema original (`Database/01_schema.sql`) não tinha as colunas obrigatórias para as regras
RN-04 e RN-18. Rode `Database/migracao_s4.sql` uma única vez:

```sql
-- já está no arquivo; resumo:
alter table eventos add column organizador_id uuid not null references usuarios(id),
                    add column estado varchar not null default 'RASCUNHO';
alter table atividades add column criterio_frequencia varchar not null default 'CHECK_IN_UNICO',
                       add column trilha_categoria varchar;
```

## Executar em outro computador

1. Instalar JDK 11+.
2. Clonar o repositório: `git clone https://github.com/Notliw-Matos/cadastroDeEventos.git`
3. Rodar `scripts\testar.bat` (sem banco) — deve passar 144 testes.
4. Para usar com banco real, definir as três variáveis de ambiente e rodar o seed.
5. `scripts\api.bat` + abrir `site/index.html`.

**Não é necessário** instalar Maven, npm, PostgreSQL local nem nenhum servidor de aplicação.
