package app;

import application.AutenticarUsuarioUseCase;
import application.CadastrarUsuarioUseCase;
import application.CriarAtividadeUseCase;
import application.CriarEventoUseCase;
import application.UsuarioResumo;
import domain.exception.DominioException;
import domain.model.Atividade;
import domain.model.Evento;
import domain.ports.AtividadeRepository;
import domain.ports.EventoRepository;
import domain.ports.UsuarioRepository;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;
import infrastructure.persistence.AtividadeRepositoryDatabase;
import infrastructure.persistence.ConexaoDatabase;
import infrastructure.persistence.EventoRepositoryDatabase;
import infrastructure.persistence.UsuarioRepositoryDatabase;
import infrastructure.security.BCryptPasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Roteiro de demonstração das entregas S2 e S3 (composition root: única classe que conhece
 * os adaptadores concretos). Executa fluxos reais contra o banco e apaga o que criou ao final.
 *
 * Pré-requisito: variável de ambiente DB_PASSWORD (veja docs/S2_S3_ENTREGA.md).
 * Use o argumento --manter para NÃO apagar os dados criados.
 */
public class DemoS2S3 {

    private static final String EMAIL_ORGANIZADOR = "juliana.lopes.andrade.1@exemplo-organizador.com";
    private static final String EMAIL_OUTRO_ORGANIZADOR = "paula.santos.barbosa.2@exemplo-organizador.com";
    private static final String SENHA_DEMO = "Demo@123";

    public static void main(String[] args) throws Exception {
        boolean manter = args.length > 0 && "--manter".equals(args[0]);

        // ---- montagem: portas de saída ligadas aos adaptadores concretos ----
        UsuarioRepository usuarios = new UsuarioRepositoryDatabase();
        EventoRepository eventos = new EventoRepositoryDatabase();
        AtividadeRepository atividades = new AtividadeRepositoryDatabase();
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();

        // ---- casos de uso (portas de entrada) ----
        CadastrarUsuarioUseCase cadastrar = new CadastrarUsuarioUseCase(usuarios, hasher);
        AutenticarUsuarioUseCase autenticar = new AutenticarUsuarioUseCase(usuarios, hasher);
        CriarEventoUseCase criarEvento = new CriarEventoUseCase(eventos, usuarios);
        CriarAtividadeUseCase criarAtividade = new CriarAtividadeUseCase(atividades, eventos, usuarios);

        String emailNovo = "demo." + System.currentTimeMillis() + "@exemplo-participante.com";
        UUID[] criados = new UUID[3]; // usuário, evento, atividade

        try {
            titulo("S3 - CADASTRO (RF-01, RN-01, RNF-05)");
            UsuarioResumo participante = executar("Cadastrar participante válido",
                    () -> cadastrar.executar("Maria Demonstração", emailNovo, "Senha123"));
            criados[0] = participante.getId();
            tentar("Cadastrar o MESMO e-mail de novo (deve falhar)",
                    () -> cadastrar.executar("Outra Pessoa", emailNovo.toUpperCase(), "Senha123"));
            tentar("Cadastrar com senha fraca (deve falhar)",
                    () -> cadastrar.executar("Fraco", "fraco@exemplo.com", "123"));

            titulo("S3 - LOGIN E AUTORIZAÇÃO (RF-02, RNF-06, RN-18)");
            executar("Login do participante recém-criado", () -> autenticar.executar(emailNovo, "Senha123"));
            tentar("Login com senha errada (deve falhar)", () -> autenticar.executar(emailNovo, "Errada123"));
            UsuarioResumo dono = executar("Login do organizador do seed (hash bcrypt do pgcrypto)",
                    () -> autenticar.executar(EMAIL_ORGANIZADOR, SENHA_DEMO));
            UsuarioResumo outro = autenticar.executar(EMAIL_OUTRO_ORGANIZADOR, SENHA_DEMO);

            LocalDateTime inicio = LocalDateTime.now().plusMonths(2).withHour(8).withMinute(0).withSecond(0).withNano(0);
            IntervaloTempo periodo = new IntervaloTempo(inicio, inicio.plusHours(10));
            tentar("Participante tenta criar evento (deve ser NEGADO)",
                    () -> criarEvento.executar(participante.getId(), "Evento Proibido", null, periodo, "Auditório"));

            titulo("S2 - EVENTO E ATIVIDADE PERSISTIDOS (RF-04, RF-05, RN-04, RN-20)");
            Evento evento = executar("Organizador cria evento (nasce em RASCUNHO)",
                    () -> criarEvento.executar(dono.getId(), "Demo S2/S3", "Evento criado pela demonstração", periodo, "Auditório Principal"));
            criados[1] = evento.getId();

            Evento relido = eventos.buscarPorId(evento.getId()).orElseThrow(AssertionError::new);
            System.out.println("     -> relido do banco: \"" + relido.getTitulo() + "\" | " + relido.getEstado()
                    + " | " + relido.getPeriodo() + " | fuso " + relido.getFusoHorario());

            Atividade atividade = executar("Organizador cria atividade com 2 vagas",
                    () -> criarAtividade.executar(dono.getId(), evento.getId(), "Oficina de POO",
                            Atividade.TipoAtividade.OFICINA, new IntervaloTempo(inicio, inicio.plusHours(2)), Vagas.limitadas(2)));
            criados[2] = atividade.getId();
            Atividade reAtividade = atividades.buscarPorId(atividade.getId()).orElseThrow(AssertionError::new);
            System.out.println("     -> relida do banco: \"" + reAtividade.getTitulo() + "\" | " + reAtividade.getTipo()
                    + " | " + reAtividade.getVagas() + " | " + reAtividade.getIntervalo());

            tentar("OUTRO organizador tenta mexer no evento alheio (deve ser NEGADO)",
                    () -> criarAtividade.executar(outro.getId(), evento.getId(), "Invasão",
                            Atividade.TipoAtividade.PALESTRA, new IntervaloTempo(inicio, inicio.plusHours(1)), Vagas.ilimitadas()));
            tentar("Período inválido: fim antes do início (deve falhar no objeto de valor)",
                    () -> new IntervaloTempo(inicio, inicio.minusHours(1)));

            System.out.println("\n[OK] Demonstração concluída.");
        } finally {
            if (manter) {
                System.out.println("(--manter: dados de demonstração preservados no banco)");
            } else {
                limpar(criados);
                System.out.println("(dados de demonstração removidos do banco)");
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println("\n=== " + texto + " ===");
    }

    /** Caminho válido: precisa funcionar. */
    private static <T> T executar(String descricao, Callable<T> acao) throws Exception {
        T resultado = acao.call();
        System.out.println("  [OK] " + descricao);
        return resultado;
    }

    /** Erro relevante: precisa falhar com uma falha DE DOMÍNIO e mensagem compreensível. */
    private static void tentar(String descricao, Callable<?> acao) throws Exception {
        try {
            acao.call();
            throw new AssertionError("Deveria ter sido rejeitado: " + descricao);
        } catch (DominioException e) {
            System.out.println("  [OK] " + descricao);
            System.out.println("     -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void limpar(UUID[] ids) throws Exception {
        String[] tabelas = {"usuarios", "eventos", "atividades"};
        try (Connection c = ConexaoDatabase.getConnection()) {
            for (int i = ids.length - 1; i >= 0; i--) {
                if (ids[i] == null) continue;
                try (PreparedStatement s = c.prepareStatement("DELETE FROM " + tabelas[i] + " WHERE id = ?")) {
                    s.setObject(1, ids[i]);
                    s.executeUpdate();
                }
            }
        }
    }
}
