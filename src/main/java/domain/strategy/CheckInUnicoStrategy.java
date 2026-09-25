package domain.strategy;

import domain.model.RegistroPresenca;
import java.util.List;

public class CheckInUnicoStrategy implements ValidadorFrequenciaStrategy {
    @Override
    public boolean isPresencaValida(List<RegistroPresenca> registros) {
        if (registros == null || registros.isEmpty()) return false;
        
        return registros.stream().anyMatch(r -> 
            r.getTipo() == RegistroPresenca.TipoMarcacao.CHECK_IN_UNICO || 
            r.getTipo() == RegistroPresenca.TipoMarcacao.MANUAL
        );
    }
}