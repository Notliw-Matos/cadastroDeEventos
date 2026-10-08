package application.ports.in;

import domain.model.Evento;
import domain.vo.IntervaloTempo;

import java.util.UUID;

/** Porta de entrada (RF-04): só organizador/administrador cria evento. */
public interface CriarEvento {
    Evento executar(UUID solicitanteId, String titulo, String descricao, IntervaloTempo periodo, String local);
}
