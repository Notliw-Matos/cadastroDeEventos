package domain.strategy;

import domain.model.Atividade;

import java.util.List;

/**
 * RN-07: "Atividades conflitantes devem ser alertadas OU bloqueadas por uma política uniforme e
 * documentada." É exatamente um ponto de variação: a mesma verificação de conflito pode terminar
 * de duas formas diferentes, sem que o caso de uso que a chama precise saber qual delas está
 * ativa (ROO-05 polimorfismo, ROO-04 composição — RealizarInscricaoUseCase é composto com uma
 * política, não decide sozinho o que fazer com um conflito).
 */
public interface PoliticaConflitoHorario {
    /**
     * Decide o que fazer quando {@code nova} conflita com uma atividade em que o participante já
     * está inscrito. Bloqueante lança {@link domain.exception.ConflitoDeHorarioException};
     * alerta-apenas acrescenta uma mensagem em {@code avisos} e deixa a inscrição prosseguir.
     */
    void verificar(Atividade nova, Atividade existente, List<String> avisos);
}
