package domain.strategy;

import domain.model.RegistroPresenca;
import java.util.List;

/** Presença válida com um único check-in (ou lançamento manual autorizado). */
public class CheckInUnicoStrategy implements ValidadorFrequenciaStrategy {
    @Override
    public boolean isPresencaValida(List<RegistroPresenca> registros) {
        if (registros == null || registros.isEmpty()) return false;

        return registros.stream().anyMatch(r ->
                r.getTipo() == RegistroPresenca.TipoMarcacao.CHECK_IN ||
                r.getTipo() == RegistroPresenca.TipoMarcacao.MANUAL);
    }
}
