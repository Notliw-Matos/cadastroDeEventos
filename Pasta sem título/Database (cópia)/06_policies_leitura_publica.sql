-- RF-10/RF-11: Visitante acessa agenda pública (eventos publicados, atividades,
-- locais e palestrantes) sem autenticação. Escrita continua bloqueada para
-- todo mundo, exceto via backend (Secret key / conexão direta do Postgres).

create policy "atividades_de_eventos_publicados_sao_publicas"
    on atividades for select
    using (
        exists (
            select 1 from eventos e
            where e.id = atividades.evento_id
              and e.estado = 'PUBLICADO'
        )
    );

create policy "locais_sao_publicos"
    on locais for select
    using (true);

create policy "palestrantes_de_atividades_publicadas_sao_publicos"
    on atividades_pessoas for select
    using (
        exists (
            select 1 from atividades a
            join eventos e on e.id = a.evento_id
            where a.id = atividades_pessoas.atividade_id
              and e.estado = 'PUBLICADO'
        )
    );

-- Nome/e-mail de organizador NÃO fica público (usuarios continua sem policy de
-- leitura), então o nome do palestrante deve vir do campo nome_pessoa em
-- atividades_pessoas, não de um join com usuarios.
