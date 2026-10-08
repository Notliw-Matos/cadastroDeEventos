package application.ports.in;

import domain.model.Atividade;
import domain.vo.IntervaloTempo;
import domain.vo.Vagas;

import java.util.UUID;

/** Porta de entrada (RF-05): quem gerencia o evento cadastra suas atividades. */
public interface CriarAtividade {
    Atividade executar(UUID solicitanteId, UUID eventoId, String titulo,
                       Atividade.TipoAtividade tipo, IntervaloTempo intervalo, Vagas vagas);
}
