package domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Inscricao {

    private final UUID id;
    private final UUID participanteId;
    private final UUID atividadeId;
    private final List<RegistroPresenca> presencas;

    public Inscricao(UUID id, UUID participanteId, UUID atividadeId) {
        this.id = id;
        this.participanteId = participanteId;
        this.atividadeId = atividadeId;
        this.presencas = new ArrayList<>();
    }

    public void adicionarPresenca(RegistroPresenca presenca) {
        this.presencas.add(presenca);
    }

    public List<RegistroPresenca> getPresencas() {
        return Collections.unmodifiableList(presencas);
    }

    public UUID getId() { 
        return id; 
    }

    public UUID getParticipanteId() { 
        return participanteId; 
    }

    public UUID getAtividadeId() { 
        return atividadeId; 
    }
}