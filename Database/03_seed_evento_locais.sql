-- Evento de demonstração + locais


insert into eventos (id, titulo, descricao, data_inicio, data_fim, local_ou_modalidade, estado, fuso_horario, organizador_id)
select
  '11111111-1111-1111-1111-111111111111'::uuid,
  'Simpósio Acadêmico de Tecnologia 2026',
  'Evento de demonstração criado para validar a plataforma de gestão de eventos.',
  '2026-10-06 08:00:00-03',
  '2026-10-08 18:00:00-03',
  'Presencial - Campus Central',
  'PUBLICADO',
  'America/Sao_Paulo',
  id
from usuarios
where perfil = 'ORGANIZADOR'
limit 1;

insert into locais (id, nome, capacidade) values
  (gen_random_uuid(), 'Sala 01', 50),
  (gen_random_uuid(), 'Sala 02', 40),
  (gen_random_uuid(), 'Sala 03', 80),
  (gen_random_uuid(), 'Sala 04', 30),
  (gen_random_uuid(), 'Sala 05', 30),
  (gen_random_uuid(), 'Sala 06', 120),
  (gen_random_uuid(), 'Sala 07', 30),
  (gen_random_uuid(), 'Sala 08', 50),
  (gen_random_uuid(), 'Sala 09', 120),
  (gen_random_uuid(), 'Sala 10', 30);
