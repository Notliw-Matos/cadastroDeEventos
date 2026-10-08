-- Reseta o schema por completo (uso: quando quiser rodar 01_schema.sql do zero de novo)
-- ATENÇÃO: isso APAGA todas as tabelas e dados criados pelos scripts anteriores.

drop table if exists certificados cascade;
drop table if exists respostas cascade;
drop table if exists opcoes_pergunta cascade;
drop table if exists perguntas cascade;
drop table if exists questionarios cascade;
drop table if exists frequencias cascade;
drop table if exists inscricoes_atividade cascade;
drop table if exists inscricoes_evento cascade;
drop table if exists atividades_pessoas cascade;
drop table if exists atividades cascade;
drop table if exists eventos cascade;
drop table if exists locais cascade;
drop table if exists usuarios cascade;

drop type if exists tipo_resposta_pergunta;
drop type if exists tipo_marcacao_frequencia;
drop type if exists criterio_frequencia;
drop type if exists situacao_inscricao;
drop type if exists papel_pessoa_atividade;
drop type if exists tipo_atividade;
drop type if exists estado_evento;
drop type if exists perfil_usuario;
