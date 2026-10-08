package domain.model;

import domain.strategy.CheckInUnicoStrategy;
import domain.strategy.EntradaSaidaStrategy;
import domain.strategy.ValidacaoManualStrategy;
import domain.strategy.ValidadorFrequenciaStrategy;

/**
 * Critério de presença configurado em cada atividade (RF-19, RN-09).
 * Cada constante carrega sua própria estratégia: quem precisa validar presença
 * pergunta ao critério, em vez de decidir com if/switch por tipo.
 */
public enum CriterioFrequencia {
    CHECK_IN_UNICO(new CheckInUnicoStrategy()),
    ENTRADA_SAIDA(new EntradaSaidaStrategy()),
    MANUAL(new ValidacaoManualStrategy());

    private final ValidadorFrequenciaStrategy validador;

    CriterioFrequencia(ValidadorFrequenciaStrategy validador) {
        this.validador = validador;
    }

    public ValidadorFrequenciaStrategy validador() {
        return validador;
    }
}
