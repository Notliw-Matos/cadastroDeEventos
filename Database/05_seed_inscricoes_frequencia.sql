-- Inscreve todos os participantes no evento de demonstração (RF-12/RF-14)
insert into inscricoes_evento (participante_id, evento_id, situacao)
select id, '11111111-1111-1111-1111-111111111111'::uuid, 'CONFIRMADA'
from usuarios
where perfil = 'PARTICIPANTE'
on conflict (participante_id, evento_id) do nothing;

-- ---------------------------------------------------------------------
-- Inscrições em atividades (RF-16), participante por participante.
--
-- CORREÇÃO: a versão anterior usava
--   "cross join lateral (select id from atividades order by random() limit N) a"
-- Como essa subquery não faz referência ao participante (p.id), o Postgres
-- pode avaliá-la UMA ÚNICA VEZ e reaproveitar o mesmo resultado pra todo
-- mundo — foi exatamente isso que aconteceu (todos os 500 caíram nas
-- mesmas 2 atividades). A versão abaixo usa um laço linha a linha, o que
-- também tem a vantagem de já respeitar os gatilhos RN-06 (vagas) e
-- RN-07 (conflito de horário) criados no banco: se a tentativa esbarrar
-- em algum dos dois, ela simplesmente tenta outra atividade.
-- ---------------------------------------------------------------------
do $$
declare
  r_participante record;
  v_atividade_id uuid;
  v_alvo         int;
  v_tentativas   int;
  v_conseguidas  int;
begin
  for r_participante in select id from usuarios where perfil = 'PARTICIPANTE' loop
    v_alvo        := (2 + floor(random() * 4))::int;  -- entre 2 e 5 atividades
    v_conseguidas := 0;
    v_tentativas  := 0;

    while v_conseguidas < v_alvo and v_tentativas < 15 loop
      v_tentativas := v_tentativas + 1;

      select id into v_atividade_id from atividades order by random() limit 1;

      begin
        insert into inscricoes_atividade (participante_id, atividade_id, situacao)
        values (r_participante.id, v_atividade_id, 'CONFIRMADA');
        v_conseguidas := v_conseguidas + 1;
      exception when others then
        -- duplicata, vagas esgotadas (RN-06) ou conflito de horário (RN-07): tenta outra
        null;
      end;
    end loop;
  end loop;
end $$;

-- Registra frequência (QR Code) para ~70% das inscrições em atividade,
-- simulando presença real (RF-21/RF-23)
insert into frequencias (participante_id, atividade_id, data_hora, tipo_marcacao)
select ia.participante_id, ia.atividade_id, at.horario_inicio + interval '5 minutes', 'QR_CODE'
from inscricoes_atividade ia
join atividades at on at.id = ia.atividade_id
where random() < 0.7;
