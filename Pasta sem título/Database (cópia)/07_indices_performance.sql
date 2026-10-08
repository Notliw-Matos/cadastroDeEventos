-- RNF-10: consultas precisam responder bem com >=500 participantes e
-- >=100 atividades. Índices cobrindo as foreign keys que ainda não tinham
-- (apontado pelo advisor de performance do Supabase).

create index if not exists idx_atividades_local on atividades(local_id);
create index if not exists idx_atividades_pessoas_atividade on atividades_pessoas(atividade_id);
create index if not exists idx_atividades_pessoas_usuario on atividades_pessoas(usuario_id);
create index if not exists idx_certificados_evento on certificados(evento_id);
create index if not exists idx_eventos_organizador on eventos(organizador_id);
create index if not exists idx_frequencias_participante on frequencias(participante_id);
create index if not exists idx_frequencias_atividade on frequencias(atividade_id);
create index if not exists idx_frequencias_registrado_por on frequencias(registrado_por);
create index if not exists idx_frequencias_participante_atividade on frequencias(participante_id, atividade_id);
create index if not exists idx_inscricoes_atividade_atividade on inscricoes_atividade(atividade_id);
create index if not exists idx_inscricoes_evento_evento on inscricoes_evento(evento_id);
create index if not exists idx_opcoes_pergunta_pergunta on opcoes_pergunta(pergunta_id);
create index if not exists idx_perguntas_questionario on perguntas(questionario_id);
create index if not exists idx_questionarios_atividade on questionarios(atividade_id);
create index if not exists idx_respostas_opcao on respostas(opcao_id);
create index if not exists idx_respostas_participante on respostas(participante_id);
