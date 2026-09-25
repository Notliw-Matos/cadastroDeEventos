-- Implementa no banco as regras de negócio que dá pra travar via constraint/trigger.
-- As que dependem de lógica de aplicação (RN-10, RN-12, RN-16, RN-17, RN-18) ficam
-- de fora de propósito — ver README.md.

-- RN-11: registro manual de frequência precisa guardar o responsável
alter table frequencias
  add constraint chk_frequencia_manual_tem_responsavel
  check (tipo_marcacao <> 'MANUAL' or registrado_por is not null);

-- RN-15: política de identificação/anonimato deve existir e ser informada antes do envio
alter table questionarios
  add column if not exists anonimo boolean not null default false;

-- RN-06: não deixar escolher atividade além das vagas (quando vagas não é null)
create or replace function trg_checar_vagas_atividade() returns trigger as $$
declare
  v_vagas integer;
  v_ocupadas integer;
begin
  select vagas into v_vagas from atividades where id = new.atividade_id;
  if v_vagas is not null then
    select count(*) into v_ocupadas
      from inscricoes_atividade
      where atividade_id = new.atividade_id and situacao = 'CONFIRMADA';
    if v_ocupadas >= v_vagas then
      raise exception 'RN-06: vagas esgotadas para esta atividade (limite: %)', v_vagas;
    end if;
  end if;
  return new;
end;
$$ language plpgsql;

create trigger trg_inscricao_atividade_vagas
before insert on inscricoes_atividade
for each row when (new.situacao = 'CONFIRMADA')
execute function trg_checar_vagas_atividade();

-- RN-07: bloquear inscrição em atividades com horário conflitante (política escolhida: bloquear)
create or replace function trg_checar_conflito_horario() returns trigger as $$
declare
  v_conflito integer;
begin
  select count(*) into v_conflito
  from inscricoes_atividade ia
  join atividades a_exist on a_exist.id = ia.atividade_id
  join atividades a_new on a_new.id = new.atividade_id
  where ia.participante_id = new.participante_id
    and ia.situacao = 'CONFIRMADA'
    and ia.atividade_id <> new.atividade_id
    and a_exist.horario_inicio < a_new.horario_fim
    and a_exist.horario_fim > a_new.horario_inicio;

  if v_conflito > 0 then
    raise exception 'RN-07: conflito de horário com outra atividade já inscrita';
  end if;
  return new;
end;
$$ language plpgsql;

create trigger trg_inscricao_atividade_conflito
before insert on inscricoes_atividade
for each row when (new.situacao = 'CONFIRMADA')
execute function trg_checar_conflito_horario();

-- RN-13: só quem está inscrito E tem presença registrada pode responder (avaliar)
-- (o "inscrito" já é garantido pela FK composta fk_participante_inscrito em frequencias)
create or replace function trg_checar_presenca_para_resposta() returns trigger as $$
declare
  v_atividade_id uuid;
  v_presente boolean;
begin
  select q.atividade_id into v_atividade_id
  from perguntas p
  join questionarios q on q.id = p.questionario_id
  where p.id = new.pergunta_id;

  select exists (
    select 1 from frequencias f
    where f.participante_id = new.participante_id
      and f.atividade_id = v_atividade_id
  ) into v_presente;

  if not v_presente then
    raise exception 'RN-13: somente participante com presença registrada nesta atividade pode avaliar';
  end if;

  return new;
end;
$$ language plpgsql;

create trigger trg_resposta_exige_presenca
before insert on respostas
for each row execute function trg_checar_presenca_para_resposta();
