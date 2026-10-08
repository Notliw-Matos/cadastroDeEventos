package application;

import domain.exception.EstadoInvalidoException;
import domain.model.Atividade;
import domain.model.CriterioFrequencia;
import domain.model.Evento;
import domain.model.Inscricao;
import domain.model.RegistroPresenca;
import domain.model.avaliacao.Pergunta;
import domain.model.avaliacao.Questionario;
import domain.model.avaliacao.TipoPergunta;
import domain.vo.Vagas;
import infrastructure.memoria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.Fabrica;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResponderQuestionarioUseCaseTest {

    private UsuarioRepositoryEmMemoria usuarios;
    private EventoRepositoryEmMemoria eventos;
    private AtividadeRepositoryEmMemoria atividades;
    private InscricaoRepositoryEmMemoria inscricoes;
    private RegistroFrequenciaRepositoryEmMemoria registros;
    private QuestionarioRepositoryEmMemoria questionarios;
    private RespostaQuestionarioRepositoryEmMemoria respostas;
    private ResponderQuestionarioUseCase casoDeUso;
    private RegistrarPresencaUseCase registrarPresenca;

    private UUID organizadorId, participanteId, outroParticipanteId;
    private Questionario questionario;
    private Pergunta perguntaTexto, perguntaEscala;
    private Inscricao inscricao;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioRepositoryEmMemoria();
        eventos = new EventoRepositoryEmMemoria();
        atividades = new AtividadeRepositoryEmMemoria();
        inscricoes = new InscricaoRepositoryEmMemoria();
        registros = new RegistroFrequenciaRepositoryEmMemoria();
        questionarios = new QuestionarioRepositoryEmMemoria();
        respostas = new RespostaQuestionarioRepositoryEmMemoria();

        var organizador = Fabrica.usuario("org@teste.com", domain.model.Usuario.Perfil.ORGANIZADOR);
        var participante = Fabrica.usuario("part@teste.com", domain.model.Usuario.Perfil.PARTICIPANTE);
        var outro = Fabrica.usuario("outro@teste.com", domain.model.Usuario.Perfil.PARTICIPANTE);
        usuarios.salvar(organizador);
        usuarios.salvar(participante);
        usuarios.salvar(outro);
        organizadorId = organizador.getId();
        participanteId = participante.getId();
        outroParticipanteId = outro.getId();

        Evento evento = Evento.novo(organizadorId, "Ev", null, Fabrica.intervalo(1, 8, 18), "x");
        eventos.salvar(evento);
        Atividade atividade = new Atividade(null, evento.getId(), "Ativ", Atividade.TipoAtividade.PALESTRA,
                null, null, Fabrica.intervalo(1, 9, 10), Vagas.ilimitadas(), CriterioFrequencia.CHECK_IN_UNICO, 0);
        atividades.salvar(atividade);

        registrarPresenca = new RegistrarPresencaUseCase(inscricoes, atividades, eventos, usuarios, registros);

        inscricao = new Inscricao(null, participanteId, atividade.getId());
        inscricoes.salvar(inscricao);

        perguntaTexto = new Pergunta(null, "Comentario", TipoPergunta.TEXTO_LIVRE, null);
        perguntaEscala = new Pergunta(null, "Nota", TipoPergunta.ESCALA_NUMERICA, null);
        questionario = new Questionario(null, atividade.getId(), "Avaliacao", List.of(perguntaTexto, perguntaEscala));
        questionarios.salvar(questionario);

        casoDeUso = new ResponderQuestionarioUseCase(respostas, questionarios, inscricoes, registros, registrarPresenca);
    }

    private void validarPresenca() {
        registros.salvar(new RegistroPresenca(null, inscricao.getId(), RegistroPresenca.TipoMarcacao.CHECK_IN, organizadorId));
    }

    private Map<UUID, String> respostasValidas() {
        return Map.of(perguntaTexto.getId(), "Otimo", perguntaEscala.getId(), "9");
    }

    @Test
    void participanteComPresencaValidaResponde() {
        validarPresenca();
        var r = casoDeUso.executar(participanteId, questionario.getId(), respostasValidas());
        assertNotNull(r.getId());
        assertEquals(2, r.getRespostas().size());
    }

    @Test
    void participanteSemInscricaoNaoResponde() {
        validarPresenca();
        assertThrows(EstadoInvalidoException.class,
                () -> casoDeUso.executar(outroParticipanteId, questionario.getId(), respostasValidas()));
    }

    @Test
    void participanteSemPresencaValidadaNaoResponde() {
        assertThrows(EstadoInvalidoException.class,
                () -> casoDeUso.executar(participanteId, questionario.getId(), respostasValidas()));
    }

    @Test
    void participanteNaoPodeResponderDuasVezes() {
        validarPresenca();
        casoDeUso.executar(participanteId, questionario.getId(), respostasValidas());
        assertThrows(EstadoInvalidoException.class,
                () -> casoDeUso.executar(participanteId, questionario.getId(), respostasValidas()));
    }
}
