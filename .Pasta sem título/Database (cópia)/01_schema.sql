-- =========================================================
-- Plataforma de Gestão de Eventos — Schema Postgres/Supabase
-- Alinhado à Especificação de Requisitos (POO II — Prof. Marcio)
-- =========================================================

create extension if not exists pgcrypto;   -- para hash de senha (RNF-05)
create extension if not exists "uuid-ossp";

-- ---------------------------------------------------------
-- ENUMS
-- ---------------------------------------------------------
create type perfil_usuario as enum ('ADMINISTRADOR', 'ORGANIZADOR', 'PARTICIPANTE');
-- Visitante NÃO tem conta (RF-01/03 e seção 3): acessa o site sem autenticação.

create type estado_evento as enum ('RASCUNHO', 'PUBLICADO', 'ENCERRADO');

create type tipo_atividade as enum ('PALESTRA', 'APRESENTACAO_ORAL', 'POSTER', 'PRODUTO', 'MESA', 'OFICINA', 'OUTRO');

create type papel_pessoa_atividade as enum ('PALESTRANTE', 'APRESENTADOR', 'RESPONSAVEL');

create type situacao_inscricao as enum ('CONFIRMADA', 'CANCELADA', 'LISTA_ESPERA');

create type criterio_frequencia as enum ('CHECK_IN_UNICO', 'ENTRADA_SAIDA', 'MANUAL');

create type tipo_marcacao_frequencia as enum ('QR_CODE', 'MANUAL');

create type tipo_resposta_pergunta as enum ('TEXTUAL', 'ESCOLHA_UNICA', 'ESCALA_NUMERICA');

-- ---------------------------------------------------------
-- USUÁRIOS (RF-01, RF-02, RF-03, RN-01, RNF-05)
-- ---------------------------------------------------------
create table usuarios (
    id              uuid primary key default gen_random_uuid(),
    nome            text not null,
    email           text not null unique,
    senha_hash      text not null,                -- gerado com crypt(senha, gen_salt('bf'))
    perfil          perfil_usuario not null,
    criado_em       timestamptz not null default now(),
    atualizado_em   timestamptz not null default now()
);

comment on column usuarios.senha_hash is 'Nunca armazenar senha em texto puro (RNF-05).';

-- ---------------------------------------------------------
-- LOCAIS (RF-07)
-- ---------------------------------------------------------
create table locais (
    id          uuid primary key default gen_random_uuid(),
    nome        text not null,
    capacidade  integer,
    observacoes text
);

-- ---------------------------------------------------------
-- EVENTOS (RF-04, RN-19, RN-20)
-- ---------------------------------------------------------
create table eventos (
    id              uuid primary key default gen_random_uuid(),
    titulo          text not null,
    descricao       text,
    data_inicio     timestamptz not null,
    data_fim        timestamptz not null,
    local_ou_modalidade text,               -- presencial/online/híbrido + local textual
    estado          estado_evento not null default 'RASCUNHO',
    fuso_horario    text not null default 'America/Sao_Paulo',
    organizador_id  uuid not null references usuarios(id),
    criado_em       timestamptz not null default now(),

    constraint chk_periodo_evento check (data_fim >= data_inicio)
);

-- ---------------------------------------------------------
-- ATIVIDADES (RF-05, RF-06, RF-07, RN-*)
-- ---------------------------------------------------------
create table atividades (
    id                  uuid primary key default gen_random_uuid(),
    evento_id           uuid not null references eventos(id) on delete cascade,
    titulo              text not null,
    tipo                tipo_atividade not null,
    trilha_categoria    text,
    local_id            uuid references locais(id),
    horario_inicio      timestamptz not null,
    horario_fim         timestamptz not null,
    vagas               integer,                          -- null = sem controle de vagas (RF-14)
    regra_inscricao     text,                              -- ex: 'INSCRICAO_NO_EVENTO_BASTA', 'ESCOLHA_ATIVIDADE'
    criterio_frequencia criterio_frequencia not null default 'CHECK_IN_UNICO',

    constraint chk_periodo_atividade check (horario_fim > horario_inicio)
);

create index idx_atividades_evento on atividades(evento_id);
create index idx_atividades_horario on atividades(horario_inicio, horario_fim);

-- ---------------------------------------------------------
-- PESSOAS x ATIVIDADES — palestrante/apresentador/responsável (RF-08)
-- ---------------------------------------------------------
create table atividades_pessoas (
    id            uuid primary key default gen_random_uuid(),
    atividade_id  uuid not null references atividades(id) on delete cascade,
    usuario_id    uuid references usuarios(id),   -- pode ser null se a pessoa não tem conta
    nome_pessoa   text not null,                   -- redundante propositalmente p/ permitir pessoa sem conta
    papel         papel_pessoa_atividade not null,
    foto_url      text
);

-- ---------------------------------------------------------
-- INSCRIÇÕES NO EVENTO E EM ATIVIDADES (RF-12..RF-18)
-- ---------------------------------------------------------
create table inscricoes_evento (
    id              uuid primary key default gen_random_uuid(),
    participante_id uuid not null references usuarios(id),
    evento_id       uuid not null references eventos(id) on delete cascade,
    situacao        situacao_inscricao not null default 'CONFIRMADA',
    criado_em       timestamptz not null default now(),

    unique (participante_id, evento_id)
);

create table inscricoes_atividade (
    id              uuid primary key default gen_random_uuid(),
    participante_id uuid not null references usuarios(id),
    atividade_id    uuid not null references atividades(id) on delete cascade,
    situacao        situacao_inscricao not null default 'CONFIRMADA',
    criado_em       timestamptz not null default now(),

    unique (participante_id, atividade_id)
);

create index idx_inscricoes_atividade_participante on inscricoes_atividade(participante_id);

-- ---------------------------------------------------------
-- FREQUÊNCIA (RF-19..RF-23)
-- ---------------------------------------------------------
create table frequencias (
    id              uuid primary key default gen_random_uuid(),
    participante_id uuid not null references usuarios(id),
    atividade_id    uuid not null references atividades(id) on delete cascade,
    data_hora       timestamptz not null default now(),
    tipo_marcacao   tipo_marcacao_frequencia not null,
    registrado_por  uuid references usuarios(id),   -- quem lançou manualmente (RF-22)

    constraint fk_participante_inscrito
        foreign key (participante_id, atividade_id)
        references inscricoes_atividade(participante_id, atividade_id)
);

-- Nota: para a constraint de FK composta acima funcionar, inscricoes_atividade
-- precisa de uma UNIQUE em (participante_id, atividade_id), já criada acima.

-- ---------------------------------------------------------
-- QUESTIONÁRIOS E AVALIAÇÕES (RF-24..RF-28, RN-13, RN-14)
-- ---------------------------------------------------------
create table questionarios (
    id            uuid primary key default gen_random_uuid(),
    atividade_id  uuid not null references atividades(id) on delete cascade,
    titulo        text not null
);

create table perguntas (
    id              uuid primary key default gen_random_uuid(),
    questionario_id uuid not null references questionarios(id) on delete cascade,
    texto           text not null,
    tipo_resposta   tipo_resposta_pergunta not null,
    ordem           integer not null default 0
);

create table opcoes_pergunta (
    id          uuid primary key default gen_random_uuid(),
    pergunta_id uuid not null references perguntas(id) on delete cascade,
    texto       text not null
);

create table respostas (
    id              uuid primary key default gen_random_uuid(),
    pergunta_id     uuid not null references perguntas(id) on delete cascade,
    participante_id uuid not null references usuarios(id),
    valor_texto     text,
    opcao_id        uuid references opcoes_pergunta(id),
    valor_escala    integer,
    criado_em       timestamptz not null default now(),

    unique (pergunta_id, participante_id)   -- RN-14: no máx. 1 resposta válida por questionário/pergunta
);

-- ---------------------------------------------------------
-- CERTIFICADOS (RF-32..RF-35, opcional/desejável)
-- ---------------------------------------------------------
create table certificados (
    id              uuid primary key default gen_random_uuid(),
    participante_id uuid not null references usuarios(id),
    evento_id       uuid not null references eventos(id) on delete cascade,
    elegivel        boolean not null default false,
    arquivo_url     text,
    emitido_em      timestamptz,
    enviado_email_em timestamptz,

    unique (participante_id, evento_id)
);

-- =========================================================
-- ROW LEVEL SECURITY
-- Ative por tabela e escreva as policies conforme os perfis.
-- Lembre-se: RNF-06 exige que a autorização REAL aconteça no
-- servidor/API, então RLS aqui é uma camada extra de defesa,
-- não substitui a checagem na sua aplicação Java.
-- =========================================================
alter table usuarios enable row level security;
alter table eventos enable row level security;
alter table atividades enable row level security;
alter table inscricoes_evento enable row level security;
alter table inscricoes_atividade enable row level security;
alter table frequencias enable row level security;
alter table questionarios enable row level security;
alter table perguntas enable row level security;
alter table respostas enable row level security;
alter table certificados enable row level security;

-- Exemplo mínimo: leitura pública de eventos publicados (site público / visitante)
create policy "eventos_publicados_sao_publicos"
    on eventos for select
    using (estado = 'PUBLICADO');

-- Sua API (rodando com a Secret key) faz o restante das operações
-- sensíveis via backend Java, que já valida o perfil do usuário
-- autenticado antes de tocar no banco.
