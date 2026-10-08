package domain.model;

import domain.exception.EstadoInvalidoException;
import domain.exception.ValorInvalidoException;
import domain.exception.VagasEsgotadasException;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;

import java.util.UUID;

public class Atividade {

    /** Espelha o enum tipo_atividade do banco (RF-05). */
    public enum TipoAtividade { PALESTRA, APRESENTACAO_ORAL, POSTER, PRODUTO, MESA, OFICINA, OUTRO }

    private final UUID id;
    private final UUID eventoId;
    private final String titulo;
    private final TipoAtividade tipo;
    private final String trilhaCategoria;
    private final String local;
    private final IntervaloTempo intervalo;
    private final Vagas vagas;
    private final CriterioFrequencia criterioFrequencia;
    private int totalInscritos;

    /** Construtor completo: usado também para reconstituir a atividade a partir do banco. */
    public Atividade(UUID id, UUID eventoId, String titulo, TipoAtividade tipo, String trilhaCategoria,
                     String local, IntervaloTempo intervalo, Vagas vagas,
                     CriterioFrequencia criterioFrequencia, int totalInscritos) {
        if (eventoId == null) throw new ValorInvalidoException("A atividade deve pertencer a um evento.");
        if (titulo == null || titulo.trim().isEmpty()) throw new ValorInvalidoException("Título é obrigatório.");
        if (tipo == null) throw new ValorInvalidoException("O tipo da atividade é obrigatório.");
        if (intervalo == null) throw new ValorInvalidoException("O horário da atividade é obrigatório.");
        if (vagas == null) throw new ValorInvalidoException("A política de vagas é obrigatória.");
        if (criterioFrequencia == null) throw new ValorInvalidoException("O critério de frequência é obrigatório.");
        if (totalInscritos < 0 || !vagas.comporta(totalInscritos)) {
            throw new ValorInvalidoException("Total de inscritos incompatível com as vagas da atividade.");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.eventoId = eventoId;
        this.titulo = titulo.trim();
        this.tipo = tipo;
        this.trilhaCategoria = trilhaCategoria;
        this.local = local;
        this.intervalo = intervalo;
        this.vagas = vagas;
        this.criterioFrequencia = criterioFrequencia;
        this.totalInscritos = totalInscritos;
    }

    /** Atividade nova: sem inscritos e com check-in único como critério de presença padrão. */
    public static Atividade nova(UUID eventoId, String titulo, TipoAtividade tipo,
                                 IntervaloTempo intervalo, Vagas vagas) {
        return new Atividade(null, eventoId, titulo, tipo, null, null, intervalo, vagas,
                CriterioFrequencia.CHECK_IN_UNICO, 0);
    }

    public boolean temVagaDisponivel() {
        return vagas.haVagaCom(totalInscritos);
    }

    /** RN-06: a única porta para ocupar uma vaga; nunca passa do limite. */
    public void incrementarInscrito() {
        if (!temVagaDisponivel()) {
            throw new VagasEsgotadasException();
        }
        this.totalInscritos++;
    }

    public void decrementarInscrito() {
        if (totalInscritos == 0) {
            throw new EstadoInvalidoException("Não há inscritos para remover desta atividade.");
        }
        this.totalInscritos--;
    }

    /** RN-07: conflito de horário entre duas atividades. */
    public boolean conflitaCom(Atividade outra) {
        return this.intervalo.temConflitoCom(outra.intervalo);
    }

    public UUID getId() { return id; }
    public UUID getEventoId() { return eventoId; }
    public String getTitulo() { return titulo; }
    public TipoAtividade getTipo() { return tipo; }
    public String getTrilhaCategoria() { return trilhaCategoria; }
    public String getLocal() { return local; }
    public IntervaloTempo getIntervalo() { return intervalo; }
    public Vagas getVagas() { return vagas; }
    public CriterioFrequencia getCriterioFrequencia() { return criterioFrequencia; }
    public int getTotalInscritos() { return totalInscritos; }
}
