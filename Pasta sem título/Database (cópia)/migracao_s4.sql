-- Migração S4: adiciona colunas obrigatórias ao schema original.
-- Execute UMA VEZ no SQL Editor do Supabase antes de rodar o seed.
-- É seguro rodar novamente (IF NOT EXISTS protege cada instrução).

-- 1. Apagar o "Evento de Teste" (não tem organizador_id, bloquearia o ALTER TABLE)
DELETE FROM eventos WHERE titulo = 'Evento de Teste';

-- 2. eventos: dono (RN-18) e estado (RN-04)
ALTER TABLE eventos
    ADD COLUMN IF NOT EXISTS organizador_id uuid REFERENCES usuarios(id),
    ADD COLUMN IF NOT EXISTS estado varchar NOT NULL DEFAULT 'RASCUNHO';

-- A coluna foi criada sem NOT NULL para não falhar em tabelas com linhas antigas.
-- Depois de garantir que todas as linhas têm um dono, adicione a restrição:
-- ALTER TABLE eventos ALTER COLUMN organizador_id SET NOT NULL;

-- 3. atividades: critério de frequência (S6) e trilha/categoria (RF-06)
ALTER TABLE atividades
    ADD COLUMN IF NOT EXISTS criterio_frequencia varchar NOT NULL DEFAULT 'CHECK_IN_UNICO',
    ADD COLUMN IF NOT EXISTS trilha_categoria varchar;

-- 4. Índices para consultas comuns (RNF-10)
CREATE INDEX IF NOT EXISTS idx_atividades_evento      ON atividades(evento_id);
CREATE INDEX IF NOT EXISTS idx_inscricoes_participante ON inscricoes(participante_id);
CREATE INDEX IF NOT EXISTS idx_inscricoes_atividade    ON inscricoes(atividade_id);
