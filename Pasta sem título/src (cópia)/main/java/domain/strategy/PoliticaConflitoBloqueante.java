package domain.strategy;

import domain.exception.ConflitoDeHorarioException;
import domain.model.Atividade;

import java.util.List;

/** Política padrão da plataforma: conflito de horário impede a inscrição (RF-18 "impedir"). */
public class PoliticaConflitoBloqueante implements PoliticaConflitoHorario {
    @Override
    public void verificar(Atividade nova, Atividade existente, List<String> avisos) {
        throw new ConflitoDeHorarioException("Conflito de horário com a atividade: " + existente.getTitulo());
    }
}
