package application;

import application.relatorio.RelatorioExportavel;
import domain.model.Atividade;
import domain.model.CriterioFrequencia;
import domain.model.Evento;
import domain.model.Inscricao;
import domain.model.RegistroPresenca;
import domain.model.Usuario;
import domain.vo.Vagas;
import infrastructure.memoria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Fabrica;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GerarRelatorioUseCaseTest {

    private UsuarioRepositoryEmMemoria usuarios;
    private InscricaoRepositoryEmMemoria inscricoes;
    private RegistroFrequenciaRepositoryEmMemoria registros;
    private GerarRelatorioUseCase casoDeUso;
    private UUID organizadorId, participanteId, outroOrganizadorId;
    private UUID atividadeId;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryEmMemoria();
        var eventos = new EventoRepositoryEmMemoria();
        var atividades = new AtividadeRepositoryEmMemoria();
        inscricoes = new InscricaoRepositoryEmMemoria();
        registros = new RegistroFrequenciaRepositoryEmMemoria();

        var organizador = Fabrica.usuario("org@teste.com", Usuario.Perfil.ORGANIZADOR);
        var outroOrg = Fabrica.usuario("outro@teste.com", Usuario.Perfil.ORGANIZADOR);
        var participante = Fabrica.usuario("part@teste.com", Usuario.Perfil.PARTICIPANTE);
        usuarios.salvar(organizador);
        usuarios.salvar(outroOrg);
        usuarios.salvar(participante);
        organizadorId = organizador.getId();
        outroOrganizadorId = outroOrg.getId();
        participanteId = participante.getId();

        Evento evento = Evento.novo(organizadorId, "Ev", null, Fabrica.intervalo(1, 8, 18), "x");
        eventos.salvar(evento);
        Atividade atividade = new Atividade(null, evento.getId(), "Palestra", Atividade.TipoAtividade.PALESTRA,
                null, null, Fabrica.intervalo(1, 9, 10), Vagas.ilimitadas(), CriterioFrequencia.CHECK_IN_UNICO, 0);
        atividades.salvar(atividade);
        atividadeId = atividade.getId();

        Inscricao inscricao = new Inscricao(null, participanteId, atividadeId);
        inscricoes.salvar(inscricao);
        registros.salvar(new RegistroPresenca(null, inscricao.getId(), RegistroPresenca.TipoMarcacao.CHECK_IN, organizadorId));

        casoDeUso = new GerarRelatorioUseCase(inscricoes, usuarios, atividades, eventos, registros);
    }

    @Test
    void relatorioInscricoesContemParticipante() {
        RelatorioExportavel rel = casoDeUso.executar(organizadorId, atividadeId, GerarRelatorioUseCase.TipoRelatorio.INSCRICOES);
        String csv = rel.toCsv();
        assertTrue(csv.contains("Palestra"));
        assertTrue(csv.contains("part@teste.com"));
        assertEquals("relatorio_inscricoes.csv", rel.nomeArquivo());
    }

    @Test
    void relatorioFrequenciaIndicaPresente() {
        RelatorioExportavel rel = casoDeUso.executar(organizadorId, atividadeId, GerarRelatorioUseCase.TipoRelatorio.FREQUENCIA);
        String csv = rel.toCsv();
        assertTrue(csv.contains("SIM"), "participante com check-in deve aparecer como SIM");
        assertEquals("relatorio_frequencia.csv", rel.nomeArquivo());
    }

    @Test
    void outroOrganizadorNaoGeraRelatorio() {
        assertThrows(domain.exception.AcessoNegadoException.class,
                () -> casoDeUso.executar(outroOrganizadorId, atividadeId, GerarRelatorioUseCase.TipoRelatorio.INSCRICOES));
    }
}
