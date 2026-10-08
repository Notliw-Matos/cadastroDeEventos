package app;

import application.CadastrarUsuarioUseCase;
import domain.exception.EmailJaCadastradoException;
import domain.model.Atividade;
import domain.model.Evento;
import domain.model.Usuario;
import domain.vo.Email;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;
import infrastructure.persistence.AtividadeRepositoryDatabase;
import infrastructure.persistence.ConexaoDatabase;
import infrastructure.persistence.EventoRepositoryDatabase;
import infrastructure.persistence.UsuarioRepositoryDatabase;
import infrastructure.security.BCryptPasswordHasher;

import java.time.LocalDateTime;

/**
 * Carga de dados de demonstração para RNF-10 ("consultas comuns devem responder de forma
 * adequada numa base com, no mínimo, 500 participantes e 100 atividades"). Usa os mesmos casos
 * de uso e o mesmo domínio da aplicação (não faz INSERT direto), então os dados gerados já
 * passam pelas mesmas regras de negócio que valeriam para um usuário real.
 *
 * Uso: defina DB_URL/DB_USER/DB_PASSWORD e rode esta classe (scripts/seed.sh ou scripts/seed.bat).
 * É seguro rodar mais de uma vez: participantes já cadastrados são só contados, não duplicados.
 * Atividades e o evento, porém, são sempre criados de novo a cada execução — rode só uma vez,
 * ou apague o evento "Semana de Tecnologia (dados de demonstração)" antes de rodar de novo.
 */
public class SeedDemo {

    private static final int TOTAL_PARTICIPANTES = 500;
    private static final int TOTAL_ATIVIDADES = 100;
    private static final String SENHA_PADRAO = "Demo1234";

    public static void main(String[] args) {
        if (!ConexaoDatabase.estaConfigurado()) {
            System.out.println("Defina DB_URL, DB_USER e DB_PASSWORD antes de rodar este script (ver docs/S4_ENTREGA.md).");
            return;
        }

        UsuarioRepositoryDatabase usuarios = new UsuarioRepositoryDatabase();
        EventoRepositoryDatabase eventos = new EventoRepositoryDatabase();
        AtividadeRepositoryDatabase atividades = new AtividadeRepositoryDatabase();
        BCryptPasswordHasher hasher = new BCryptPasswordHasher();
        CadastrarUsuarioUseCase cadastrar = new CadastrarUsuarioUseCase(usuarios, hasher);

        Usuario organizador = obterOuCriarOrganizador(usuarios, hasher);
        System.out.println("Organizador: " + organizador.getEmail() + " / " + SENHA_PADRAO);

        int criados = 0, jaExistiam = 0;
        for (int i = 1; i <= TOTAL_PARTICIPANTES; i++) {
            String email = "participante" + i + "@seed-demo.com";
            try {
                cadastrar.executar("Participante Demo " + i, email, SENHA_PADRAO);
                criados++;
            } catch (EmailJaCadastradoException jaExiste) {
                jaExistiam++;
            }
            if (i % 100 == 0) System.out.println("... " + i + "/" + TOTAL_PARTICIPANTES + " participantes");
        }
        System.out.println("Participantes: " + criados + " criados, " + jaExistiam + " já existiam.");

        LocalDateTime base = LocalDateTime.now().plusDays(30).withHour(8).withMinute(0).withSecond(0).withNano(0);
        Evento evento = Evento.novo(organizador.getId(), "Semana de Tecnologia (dados de demonstração)",
                "Evento gerado por SeedDemo para atender RNF-10.",
                new IntervaloTempo(base, base.plusDays(4).plusHours(10)), "Vários auditórios");
        evento.publicar();
        eventos.salvar(evento);

        Atividade.TipoAtividade[] tipos = Atividade.TipoAtividade.values();
        for (int i = 1; i <= TOTAL_ATIVIDADES; i++) {
            int dia = (i - 1) % 5;
            int horaInicio = 8 + ((i - 1) % 8); // 8h às 15h, uma por hora, rotacionando
            LocalDateTime inicio = base.plusDays(dia).withHour(horaInicio);
            Vagas vagas = (i % 3 == 0) ? Vagas.ilimitadas() : Vagas.limitadas(20 + (i % 5) * 10);
            Atividade atividade = Atividade.nova(evento.getId(), "Atividade demo " + i,
                    tipos[i % tipos.length], new IntervaloTempo(inicio, inicio.plusHours(1)), vagas);
            atividades.salvar(atividade);
            if (i % 20 == 0) System.out.println("... " + i + "/" + TOTAL_ATIVIDADES + " atividades");
        }
        System.out.println("Atividades: " + TOTAL_ATIVIDADES + " criadas no evento " + evento.getId());
        System.out.println("Pronto. RNF-10 atendido: >= " + TOTAL_PARTICIPANTES + " participantes, >= " + TOTAL_ATIVIDADES + " atividades.");
    }

    private static Usuario obterOuCriarOrganizador(UsuarioRepositoryDatabase usuarios, BCryptPasswordHasher hasher) {
        Email email = new Email("organizador.seed@evento.com");
        return usuarios.buscarPorEmail(email).orElseGet(() -> {
            Usuario novo = new Usuario(null, "Organizador Seed", email, hasher.hash(SENHA_PADRAO), Usuario.Perfil.ORGANIZADOR);
            usuarios.salvar(novo);
            return novo;
        });
    }
}
