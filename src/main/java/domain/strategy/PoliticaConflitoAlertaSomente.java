package domain.strategy;

import domain.model.Atividade;

import java.util.List;

/**
 * Política alternativa (RF-18 "alertar"): deixa o participante se inscrever mesmo com conflito,
 * mas devolve um aviso explicando a sobreposição. Útil, por exemplo, quando a atividade conflitante
 * é opcional/gravada. Não é a política padrão da plataforma — ver decisão D-06 em docs/S5_ENTREGA.md.
 */
public class PoliticaConflitoAlertaSomente implements PoliticaConflitoHorario {
    @Override
    public void verificar(Atividade nova, Atividade existente, List<String> avisos) {
        avisos.add("Atenção: esta atividade tem horário sobreposto com \"" + existente.getTitulo() + "\".");
    }
}
