package domain.model;

import domain.exception.EstadoInvalidoException;
import domain.exception.ValorInvalidoException;
import domain.exception.VagasEsgotadasException;
import domain.vo.Vagas;
import org.junit.jupiter.api.Test;
import support.Fabrica;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AtividadeTest {
    private final UUID evento = UUID.randomUUID();

    private Atividade oficina(int limite, int horaInicio, int horaFim) {
        return Atividade.nova(evento, "Oficina POO", Atividade.TipoAtividade.OFICINA,
                Fabrica.intervalo(1, horaInicio, horaFim), Vagas.limitadas(limite));
    }

    @Test
    void invarianteDeVagasNuncaEUltrapassada() {
        Atividade atividade = oficina(1, 9, 11);
        atividade.incrementarInscrito();
        assertFalse(atividade.temVagaDisponivel());
        assertThrows(VagasEsgotadasException.class, atividade::incrementarInscrito);
        assertEquals(1, atividade.getTotalInscritos());
    }

    @Test
    void atividadeSemControleDeVagasNuncaEsgota() {
        Atividade livre = Atividade.nova(evento, "Palestra", Atividade.TipoAtividade.PALESTRA,
                Fabrica.intervalo(1, 9, 10), Vagas.ilimitadas());
        for (int i = 0; i < 1000; i++) livre.incrementarInscrito();
        assertTrue(livre.temVagaDisponivel());
    }

    @Test
    void liberarVagaPermiteNovaInscricao() {
        Atividade atividade = oficina(1, 9, 11);
        atividade.incrementarInscrito();
        atividade.decrementarInscrito();
        assertTrue(atividade.temVagaDisponivel());
    }

    @Test
    void naoDecrementaAbaixoDeZero() {
        assertThrows(EstadoInvalidoException.class, () -> oficina(5, 9, 11).decrementarInscrito());
    }

    @Test
    void tituloTipoHorarioEVagasSaoObrigatorios() {
        assertThrows(ValorInvalidoException.class, () -> Atividade.nova(evento, " ", Atividade.TipoAtividade.OFICINA,
                Fabrica.intervalo(1, 9, 10), Vagas.ilimitadas()));
        assertThrows(ValorInvalidoException.class, () -> Atividade.nova(evento, "x", null,
                Fabrica.intervalo(1, 9, 10), Vagas.ilimitadas()));
        assertThrows(ValorInvalidoException.class, () -> Atividade.nova(evento, "x", Atividade.TipoAtividade.OFICINA,
                null, Vagas.ilimitadas()));
        assertThrows(ValorInvalidoException.class, () -> Atividade.nova(evento, "x", Atividade.TipoAtividade.OFICINA,
                Fabrica.intervalo(1, 9, 10), null));
        assertThrows(ValorInvalidoException.class, () -> Atividade.nova(null, "x", Atividade.TipoAtividade.OFICINA,
                Fabrica.intervalo(1, 9, 10), Vagas.ilimitadas()));
    }

    @Test
    void reconstituicaoNaoAceitaMaisInscritosQueVagas() {
        assertThrows(ValorInvalidoException.class, () -> new Atividade(null, evento, "x",
                Atividade.TipoAtividade.OFICINA, null, null, Fabrica.intervalo(1, 9, 10),
                Vagas.limitadas(2), CriterioFrequencia.CHECK_IN_UNICO, 3));
    }

    @Test
    void reconstituicaoPreservaOTotalDeInscritos() {
        Atividade a = new Atividade(null, evento, "x", Atividade.TipoAtividade.OFICINA, null, null,
                Fabrica.intervalo(1, 9, 10), Vagas.limitadas(2), CriterioFrequencia.ENTRADA_SAIDA, 2);
        assertEquals(2, a.getTotalInscritos());
        assertFalse(a.temVagaDisponivel());
    }

    @Test
    void detectaConflitoDeHorarioEntreAtividades() {
        assertTrue(oficina(5, 9, 11).conflitaCom(oficina(5, 10, 12)));
        assertFalse(oficina(5, 9, 10).conflitaCom(oficina(5, 10, 12)));
    }

    @Test
    void novaAtividadeUsaCheckInUnicoPorPadrao() {
        assertEquals(CriterioFrequencia.CHECK_IN_UNICO, oficina(5, 9, 10).getCriterioFrequencia());
    }
}
