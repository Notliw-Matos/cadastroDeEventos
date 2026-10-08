-- 100 atividades de demonstração vinculadas ao evento (RNF-10 pede no mínimo 100)
do $$
declare
  v_evento_id uuid := '11111111-1111-1111-1111-111111111111';
  v_local_ids uuid[];
  v_local uuid;
  v_dia int;
  v_hora int;
  v_inicio timestamptz;
begin
  select array_agg(id) into v_local_ids from locais;

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Arquitetura de Software #1', 'MESA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Machine Learning #2', 'APRESENTACAO_ORAL', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Machine Learning #3', 'OFICINA', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Testes Automatizados #4', 'OUTRO', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Arquitetura de Software #5', 'MESA', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Privacidade de Dados #6', 'POSTER', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Design Thinking #7', 'PRODUTO', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Robótica #8', 'POSTER', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Robótica #9', 'POSTER', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Cloud Computing #10', 'PALESTRA', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Blockchain #11', 'PRODUTO', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Privacidade de Dados #12', 'POSTER', 'UX e Produto', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Machine Learning #13', 'POSTER', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Robótica #14', 'OFICINA', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Cloud Computing #15', 'PALESTRA', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Cloud Computing #16', 'PALESTRA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Cloud Computing #17', 'PRODUTO', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em IoT #18', 'POSTER', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Cloud Computing #19', 'POSTER', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Design Thinking #20', 'APRESENTACAO_ORAL', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Testes Automatizados #21', 'POSTER', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Robótica #22', 'MESA', 'UX e Produto', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Testes Automatizados #23', 'PRODUTO', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de APIs REST #24', 'OFICINA', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Privacidade de Dados #25', 'APRESENTACAO_ORAL', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em DevOps #26', 'MESA', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Cloud Computing #27', 'OUTRO', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '17:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Arquitetura de Software #28', 'MESA', 'UX e Produto', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Banco de Dados #29', 'PRODUTO', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Design Thinking #30', 'OFICINA', 'UX e Produto', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Cloud Computing #31', 'PALESTRA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Robótica #32', 'OUTRO', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '17:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Startups #33', 'APRESENTACAO_ORAL', 'UX e Produto', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '17:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Blockchain #34', 'POSTER', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Startups #35', 'OFICINA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Banco de Dados #36', 'PALESTRA', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a DevOps #37', 'POSTER', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de DevOps #38', 'APRESENTACAO_ORAL', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Acessibilidade Digital #39', 'MESA', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '17:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Blockchain #40', 'OUTRO', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Blockchain #41', 'PRODUTO', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Testes Automatizados #42', 'OUTRO', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Cloud Computing #43', 'APRESENTACAO_ORAL', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Cloud Computing #44', 'MESA', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Startups #45', 'PALESTRA', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Machine Learning #46', 'OUTRO', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Banco de Dados #47', 'MESA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '17:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Computação Gráfica #48', 'PALESTRA', 'UX e Produto', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Startups #49', 'PRODUTO', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Blockchain #50', 'OUTRO', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de DevOps #51', 'PRODUTO', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Acessibilidade Digital #52', 'MESA', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Blockchain #53', 'APRESENTACAO_ORAL', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Design Thinking #54', 'MESA', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Design Thinking #55', 'MESA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Acessibilidade Digital #56', 'MESA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Testes Automatizados #57', 'APRESENTACAO_ORAL', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Arquitetura de Software #58', 'OFICINA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Robótica #59', 'OUTRO', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Design Thinking #60', 'APRESENTACAO_ORAL', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Computação Gráfica #61', 'PRODUTO', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Testes Automatizados #62', 'MESA', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de DevOps #63', 'PALESTRA', 'UX e Produto', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em DevOps #64', 'OFICINA', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Acessibilidade Digital #65', 'MESA', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Acessibilidade Digital #66', 'POSTER', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Testes Automatizados #67', 'OUTRO', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Arquitetura de Software #68', 'MESA', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Machine Learning #69', 'PRODUTO', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Banco de Dados #70', 'PALESTRA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Privacidade de Dados #71', 'POSTER', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Cloud Computing #72', 'OFICINA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Startups #73', 'APRESENTACAO_ORAL', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre DevOps #74', 'MESA', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Startups #75', 'PALESTRA', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Startups #76', 'PALESTRA', 'Segurança da Informação', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '17:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de DevOps #77', 'OFICINA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de IoT #78', 'PRODUTO', 'UX e Produto', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Cloud Computing #79', 'OFICINA', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Robótica #80', 'OUTRO', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Design Thinking #81', 'APRESENTACAO_ORAL', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '10:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Machine Learning #82', 'POSTER', 'UX e Produto', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Testes Automatizados #83', 'APRESENTACAO_ORAL', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Startups #84', 'MESA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Testes Automatizados #85', 'OUTRO', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre Computação Gráfica #86', 'PALESTRA', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tópicos avançados em Blockchain #87', 'MESA', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Estudo de caso: Machine Learning #88', 'APRESENTACAO_ORAL', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de IoT #89', 'OFICINA', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Cloud Computing #90', 'PALESTRA', 'Inteligência Artificial', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Computação Gráfica #91', 'PRODUTO', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Banco de Dados #92', 'OFICINA', 'Dados e Analytics', v_local, v_inicio, v_inicio + interval '1 hour', null, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Avanços em Robótica #93', 'OFICINA', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '17:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Painel sobre APIs REST #94', 'OUTRO', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '09:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em IoT #95', 'APRESENTACAO_ORAL', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 80, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Privacidade de Dados #96', 'OFICINA', 'Empreendedorismo', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 2) + time '16:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Introdução a Banco de Dados #97', 'PALESTRA', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '14:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Fundamentos de Design Thinking #98', 'MESA', 'Redes e Sistemas', v_local, v_inicio, v_inicio + interval '1 hour', 50, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 1) + time '11:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Oficina prática de Computação Gráfica #99', 'OFICINA', 'Engenharia de Software', v_local, v_inicio, v_inicio + interval '1 hour', 100, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');

  v_local := v_local_ids[1 + floor(random() * array_length(v_local_ids,1))::int];
  v_inicio := ('2026-10-06'::date + 0) + time '15:00:00';
  insert into atividades (evento_id, titulo, tipo, trilha_categoria, local_id, horario_inicio, horario_fim, vagas, regra_inscricao, criterio_frequencia)
  values (v_evento_id, 'Tendências em Design Thinking #100', 'POSTER', 'Sustentabilidade Tech', v_local, v_inicio, v_inicio + interval '1 hour', 30, 'ESCOLHA_ATIVIDADE', 'CHECK_IN_UNICO');
end $$;
