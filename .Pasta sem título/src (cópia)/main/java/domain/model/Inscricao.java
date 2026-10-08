package domain.model;

import domain.exception.EstadoInvalidoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Inscricao {

    /** Espelha o enum situacao_inscricao do banco. */
    public enum Situacao { CONFIRMADA, CANCELADA, LISTA_ESPERA }

    private final UUID id;
    private final UUID participanteId;
    private final UUID atividadeId;
    private Situacao situacao;
    private final List<RegistroPresenca> presencas;

    public Inscricao(UUID id, UUID participanteId, UUID atividadeId) {
        this(id, participanteId, atividadeId, Situacao.CONFIRMADA);
    }

    public Inscricao(UUID id, UUID participanteId, UUID atividadeId, Situacao situacao) {
        this.id = id != null ? id : UUID.randomUUID();
        this.participanteId = participanteId;
        this.atividadeId = atividadeId;
        this.situacao = situacao;
        this.presencas = new ArrayList<>();
    }

    public boolean isAtiva() {
        return situacao == Situacao.CONFIRMADA;
    }

    public void cancelar() {
        if (situacao == Situacao.CANCELADA) {
            throw new EstadoInvalidoException("A inscrição já está cancelada.");
        }
        this.situacao = Situacao.CANCELADA;
    }

    public void adicionarPresenca(RegistroPresenca presenca) {
        this.presencas.add(presenca);
    }

    public List<RegistroPresenca> getPresencas() {
        return Collections.unmodifiableList(presencas);
    }

    public UUID getId() { return id; }
    public UUID getParticipanteId() { return participanteId; }
    public UUID getAtividadeId() { return atividadeId; }
    public Situacao getSituacao() { return situacao; }
}
