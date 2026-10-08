package domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegistroPresenca {

    public enum TipoMarcacao {
        CHECK_IN,
        CHECK_OUT,
        MANUAL
    }

    private final UUID id;
    private final UUID inscricaoId;
    private final TipoMarcacao tipo;
    private final LocalDateTime dataHora;
    private final UUID operadorId;

    // Construtor usado ao criar um registro novo (RF-20/RF-21/RF-22): a data/hora é sempre "agora".
    public RegistroPresenca(UUID id, UUID inscricaoId, TipoMarcacao tipo, UUID operadorId) {
        this(id, inscricaoId, tipo, LocalDateTime.now(), operadorId);
    }

    // Construtor de reconstituição (vindo do banco): usa a data/hora que já foi persistida,
    // em vez de "agora" — sem isto, todo registro lido do banco pareceria ter acabado de acontecer.
    public RegistroPresenca(UUID id, UUID inscricaoId, TipoMarcacao tipo, LocalDateTime dataHora, UUID operadorId) {
        this.id = id != null ? id : UUID.randomUUID();
        this.inscricaoId = inscricaoId;
        this.tipo = tipo;
        this.dataHora = dataHora;
        this.operadorId = operadorId;
    }

    public UUID getId() { 
        return id; 
    }

    public UUID getInscricaoId() { 
        return inscricaoId; 
    }

    public TipoMarcacao getTipo() { 
        return tipo; 
    }

    public LocalDateTime getDataHora() { 
        return dataHora; 
    }

    public UUID getOperadorId() { 
        return operadorId; 
    }
}