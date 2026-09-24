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

    // Construtor completo usado pelo Caso de Uso
    public RegistroPresenca(UUID id, UUID inscricaoId, TipoMarcacao tipo, UUID operadorId) {
        this.id = id != null ? id : UUID.randomUUID();
        this.inscricaoId = inscricaoId;
        this.tipo = tipo;
        this.dataHora = LocalDateTime.now();
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