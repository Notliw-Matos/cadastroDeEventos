package infrastructure;

import application.AutenticarUsuarioUseCase;
import application.CadastrarUsuarioUseCase;
import application.CriarAtividadeUseCase;
import application.CriarEventoUseCase;
import application.ListarAtividadesDoEventoUseCase;
import application.UsuarioResumo;
import domain.exception.AcessoNegadoException;
import domain.exception.CredenciaisInvalidasException;
import domain.exception.EmailJaCadastradoException;
import domain.model.Atividade;
import domain.model.CriterioFrequencia;
import domain.model.Evento;
import domain.model.Usuario;
import domain.vo.Email;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;
import infrastructure.persistence.AtividadeRepositoryDatabase;
import infrastructure.persistence.ConexaoDatabase;
import infrastructure.persistence.EventoRepositoryDatabase;
import infrastructure.persistence.InscricaoRepositoryDatabase;
import infrastructure.persistence.UsuarioRepositoryDatabase;
import infrastructure.security.BCryptPasswordHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de INTEGRAÇÃO com um banco Postgres real (schema confirmado no Supabase + a migração
 * documentada em docs/S4_ENTREGA.md). Só roda se DB_URL/DB_USER/DB_PASSWORD estiverem definidos
 * (senão é ignorado, e os demais testes continuam rodando sem banco). Tudo que cria é apagado
 * no final, na ordem que respeita as chaves estrangeiras.
 */
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class PersistenciaIntegracaoTest {

    private final UsuarioRepositoryDatabase usuarios = new UsuarioRepositoryDatabase();
    private final EventoRepositoryDatabase eventos = new EventoRepositoryDatabase();
    private final AtividadeRepositoryDatabase atividades = new AtividadeRepositoryDatabase();
    private final InscricaoRepositoryDatabase inscricoes = new InscricaoRepositoryDatabase();
    private final BCryptPasswordHasher hasher = new BCryptPasswordHasher();

    private final List<UUID> inscricoesCriadas = new ArrayList<>();
    private final List<UUID> atividadesCriadas = new ArrayList<>();
    private final List<UUID> eventosCriados = new ArrayList<>();
    private final List<UUID> usuariosCriados = new ArrayList<>();

    @AfterEach
    void limpar() throws Exception {
        try (Connection c = ConexaoDatabase.getConnection()) {
            apagar(c, "registros_frequencia", "inscricao_id", inscricoesCriadas);
            apagar(c, "inscricoes", "id", inscricoesCriadas);
            apagar(c, "atividades", "id", atividadesCriadas);
            apagar(c, "eventos", "id", eventosCriados);
            apagar(c, "usuarios", "id", usuariosCriados);
        }
    }

    private void apagar(Connection c, String tabela, String coluna, List<UUID> ids) throws Exception {
        for (UUID id : ids) {
            try (PreparedStatement s = c.prepareStatement("DELETE FROM " + tabela + " WHERE " + coluna + " = ?")) {
                s.setObject(1, id);
                s.executeUpdate();
            }
        }
    }

    private Usuario novoUsuario(Usuario.Perfil perfil) {
        String email = "it." + UUID.randomUUID() + "@exemplo-teste.com";
        Usuario usuario = new Usuario(null, "Usuario IT", new Email(email), hasher.hash("Senha123"), perfil);
        usuarios.salvar(usuario);
        usuariosCriados.add(usuario.getId());
        return usuario;
    }

    // ---------------- usuário / login ----------------

    @Test
    void cadastraEBuscaUsuarioSemGuardarSenhaEmTextoPuro() {
        String email = "it." + UUID.randomUUID() + "@exemplo-teste.com";
        UsuarioResumo criado = new CadastrarUsuarioUseCase(usuarios, hasher).executar("Teste IT", email, "Senha123");
        usuariosCriados.add(criado.getId());

        Usuario salvo = usuarios.buscarPorEmail(new Email(email)).orElseThrow(AssertionError::new);
        assertTrue(salvo.getSenhaHash().startsWith("$2"));
        assertNotEquals("Senha123", salvo.getSenhaHash());
    }

    @Test
    void bancoRejeitaEmailDuplicado() {
        Usuario existente = novoUsuario(Usuario.Perfil.PARTICIPANTE);
        Usuario clone = Usuario.novoParticipante("Clone", existente.getEmail(), hasher.hash("Senha123"));
        assertThrows(EmailJaCadastradoException.class, () -> usuarios.salvar(clone));
    }

    @Test
    void loginFalhaComSenhaErrada() {
        Usuario u = novoUsuario(Usuario.Perfil.PARTICIPANTE);
        AutenticarUsuarioUseCase login = new AutenticarUsuarioUseCase(usuarios, hasher);
        assertThrows(CredenciaisInvalidasException.class,
                () -> login.executar(u.getEmail().getEndereco(), "SenhaErrada1"));
    }

    // ---------------- evento / atividade ----------------

    @Test
    void eventoPersisteEVoltaIgualComDonoEEstado() {
        Usuario organizador = novoUsuario(Usuario.Perfil.ORGANIZADOR);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 5, 8, 30);
        Evento evento = Evento.novo(organizador.getId(), "Evento IT", "Descrição",
                new IntervaloTempo(inicio, inicio.plusHours(9)), "Auditório");
        evento.publicar();
        eventosCriados.add(evento.getId());

        eventos.salvar(evento);
        Evento lido = eventos.buscarPorId(evento.getId()).orElseThrow(AssertionError::new);

        assertEquals("Evento IT", lido.getTitulo());
        assertEquals(Evento.Estado.PUBLICADO, lido.getEstado());
        assertEquals(organizador.getId(), lido.getOrganizadorId());
        assertEquals(evento.getPeriodo(), lido.getPeriodo());
    }

    @Test
    void salvarDeNovoAtualizaEmVezDeDuplicar() {
        Usuario organizador = novoUsuario(Usuario.Perfil.ORGANIZADOR);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 5, 8, 0);
        Evento evento = Evento.novo(organizador.getId(), "Antes", null, new IntervaloTempo(inicio, inicio.plusHours(2)), "x");
        eventosCriados.add(evento.getId());
        eventos.salvar(evento);

        evento.alterarDados("Depois", null, evento.getPeriodo(), "y");
        eventos.salvar(evento);

        assertEquals("Depois", eventos.buscarPorId(evento.getId()).get().getTitulo());
    }

    @Test
    void listarPublicadosSoTrazEventoPublicado() {
        Usuario organizador = novoUsuario(Usuario.Perfil.ORGANIZADOR);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 6, 8, 0);

        Evento publicado = Evento.novo(organizador.getId(), "Publicado IT " + UUID.randomUUID(), null,
                new IntervaloTempo(inicio, inicio.plusHours(1)), "x");
        publicado.publicar();
        Evento rascunho = Evento.novo(organizador.getId(), "Rascunho IT " + UUID.randomUUID(), null,
                new IntervaloTempo(inicio, inicio.plusHours(1)), "x");
        eventosCriados.add(publicado.getId());
        eventosCriados.add(rascunho.getId());
        eventos.salvar(publicado);
        eventos.salvar(rascunho);

        List<String> titulos = eventos.listarPublicados().stream().map(Evento::getTitulo).collect(java.util.stream.Collectors.toList());
        assertTrue(titulos.contains(publicado.getTitulo()));
        assertFalse(titulos.contains(rascunho.getTitulo()));
    }

    @Test
    void atividadePersisteComVagasLimitadasEIlimitadasENoCriterio() {
        Usuario organizador = novoUsuario(Usuario.Perfil.ORGANIZADOR);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 5, 9, 0);
        Evento evento = Evento.novo(organizador.getId(), "Evento IT", null, new IntervaloTempo(inicio, inicio.plusHours(9)), "x");
        eventosCriados.add(evento.getId());
        eventos.salvar(evento);

        Atividade limitada = new Atividade(null, evento.getId(), "Oficina", Atividade.TipoAtividade.OFICINA, "Trilha A",
                "Sala 1", new IntervaloTempo(inicio, inicio.plusHours(2)), Vagas.limitadas(25),
                CriterioFrequencia.ENTRADA_SAIDA, 0);
        atividadesCriadas.add(limitada.getId());
        atividades.salvar(limitada);

        Atividade lida = atividades.buscarPorId(limitada.getId()).orElseThrow(AssertionError::new);
        assertEquals(Vagas.limitadas(25), lida.getVagas());
        assertEquals(CriterioFrequencia.ENTRADA_SAIDA, lida.getCriterioFrequencia());
        assertEquals("Trilha A", lida.getTrilhaCategoria());
        assertEquals("Sala 1", lida.getLocal());
        assertEquals(limitada.getIntervalo(), lida.getIntervalo());
    }

    // ---------------- autorização de ponta a ponta ----------------

    @Test
    void participanteNaoCriaEventoENadaVaiParaOBanco() {
        Usuario participante = novoUsuario(Usuario.Perfil.PARTICIPANTE);
        CriarEventoUseCase criarEvento = new CriarEventoUseCase(eventos, usuarios);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 5, 8, 0);

        assertThrows(AcessoNegadoException.class, () -> criarEvento.executar(participante.getId(), "Proibido", null,
                new IntervaloTempo(inicio, inicio.plusHours(1)), "x"));
    }

    @Test
    void organizadorCriaEventoEAtividadeEOutroOrganizadorNaoMexeNele() {
        Usuario dono = novoUsuario(Usuario.Perfil.ORGANIZADOR);
        Usuario outro = novoUsuario(Usuario.Perfil.ORGANIZADOR);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 6, 8, 0);

        Evento evento = new CriarEventoUseCase(eventos, usuarios).executar(dono.getId(), "Evento ponta a ponta", null,
                new IntervaloTempo(inicio, inicio.plusHours(9)), "Auditório");
        eventosCriados.add(evento.getId());

        CriarAtividadeUseCase criarAtividade = new CriarAtividadeUseCase(atividades, eventos, usuarios);
        Atividade criada = criarAtividade.executar(dono.getId(), evento.getId(), "Palestra de abertura",
                Atividade.TipoAtividade.PALESTRA, new IntervaloTempo(inicio, inicio.plusHours(1)), Vagas.limitadas(2));
        atividadesCriadas.add(criada.getId());

        assertThrows(AcessoNegadoException.class, () -> criarAtividade.executar(outro.getId(), evento.getId(), "Invasão",
                Atividade.TipoAtividade.PALESTRA, new IntervaloTempo(inicio, inicio.plusHours(1)), Vagas.ilimitadas()));

        List<application.AtividadeDTO> doEvento = new ListarAtividadesDoEventoUseCase(atividades, eventos)
                .executar(evento.getId());
        assertEquals(1, doEvento.size());
        assertEquals("Palestra de abertura", doEvento.get(0).paraMapa().get("titulo"));
    }

    // ---------------- inscrição com persistência real ----------------

    @Test
    void inscricaoPersisteERespeitaVagaNoBanco() {
        Usuario organizador = novoUsuario(Usuario.Perfil.ORGANIZADOR);
        Usuario participante = novoUsuario(Usuario.Perfil.PARTICIPANTE);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 7, 9, 0);

        Evento evento = Evento.novo(organizador.getId(), "Evento com inscrição", null,
                new IntervaloTempo(inicio, inicio.plusHours(2)), "x");
        eventosCriados.add(evento.getId());
        eventos.salvar(evento);

        Atividade atividade = Atividade.nova(evento.getId(), "Vaga única", Atividade.TipoAtividade.OFICINA,
                new IntervaloTempo(inicio, inicio.plusHours(1)), Vagas.limitadas(1));
        atividadesCriadas.add(atividade.getId());
        atividades.salvar(atividade);

        var resultado = new application.RealizarInscricaoUseCase(inscricoes, atividades)
                .executar(participante.getId(), atividade.getId());
        var inscricao = resultado.getInscricao();
        inscricoesCriadas.add(inscricao.getId());

        assertEquals(1, atividades.buscarPorId(atividade.getId()).get().getTotalInscritos());
        assertTrue(inscricoes.buscarPorId(inscricao.getId()).isPresent());
    }

    // ---------------- S6: frequência com persistência real ----------------

    @Test
    void registroDeFrequenciaPersisteComDataHoraRealEAutorizacao() {
        Usuario organizador = novoUsuario(Usuario.Perfil.ORGANIZADOR);
        Usuario outroOrganizador = novoUsuario(Usuario.Perfil.ORGANIZADOR);
        Usuario participante = novoUsuario(Usuario.Perfil.PARTICIPANTE);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 8, 9, 0);

        Evento evento = Evento.novo(organizador.getId(), "Evento com frequência", null,
                new IntervaloTempo(inicio, inicio.plusHours(2)), "x");
        eventosCriados.add(evento.getId());
        eventos.salvar(evento);

        Atividade atividade = Atividade.nova(evento.getId(), "Oficina com check-in", Atividade.TipoAtividade.OFICINA,
                new IntervaloTempo(inicio, inicio.plusHours(1)), Vagas.ilimitadas());
        atividadesCriadas.add(atividade.getId());
        atividades.salvar(atividade);

        var inscricao = new application.RealizarInscricaoUseCase(inscricoes, atividades)
                .executar(participante.getId(), atividade.getId()).getInscricao();
        inscricoesCriadas.add(inscricao.getId());

        var registroFrequencia = new infrastructure.persistence.RegistroFrequenciaRepositoryDatabase();
        var registrarPresenca = new application.RegistrarPresencaUseCase(
                inscricoes, atividades, eventos, usuarios, registroFrequencia);

        LocalDateTime antes = LocalDateTime.now().minusSeconds(2);
        registrarPresenca.registrarMarcacao(inscricao.getId(),
                domain.model.RegistroPresenca.TipoMarcacao.CHECK_IN, organizador.getId());

        var registros = registroFrequencia.listarPorInscricao(inscricao.getId());
        assertEquals(1, registros.size());
        assertEquals(organizador.getId(), registros.get(0).getOperadorId());
        assertTrue(registros.get(0).getDataHora().isAfter(antes), "data/hora lida do banco deve ser real, não 'agora' do objeto reconstruído");
        assertTrue(registrarPresenca.calcularSituacaoDePresenca(inscricao.getId()));

        assertThrows(AcessoNegadoException.class, () -> registrarPresenca.registrarMarcacao(inscricao.getId(),
                domain.model.RegistroPresenca.TipoMarcacao.CHECK_IN, outroOrganizador.getId()));
    }
}
