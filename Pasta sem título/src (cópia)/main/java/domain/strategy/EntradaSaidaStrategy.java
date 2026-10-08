package domain.strategy;

import domain.model.RegistroPresenca;
import java.util.List;

/** Presença válida só com entrada E saída registradas (ou lançamento manual autorizado). */
public class EntradaSaidaStrategy implements ValidadorFrequenciaStrategy {
    @Override
    public boolean isPresencaValida(List<RegistroPresenca> registros) {
        if (registros == null || registros.isEmpty()) return false;

        boolean manual = registros.stream().anyMatch(r -> r.getTipo() == RegistroPresenca.TipoMarcacao.MANUAL);
        boolean temEntrada = registros.stream().anyMatch(r -> r.getTipo() == RegistroPresenca.TipoMarcacao.CHECK_IN);
        boolean temSaida = registros.stream().anyMatch(r -> r.getTipo() == RegistroPresenca.TipoMarcacao.CHECK_OUT);

        return manual || (temEntrada && temSaida);
    }
}
