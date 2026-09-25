package domain.strategy;

import domain.model.RegistroPresenca;
import java.util.List;

public interface ValidadorFrequenciaStrategy {
    boolean isPresencaValida(List<RegistroPresenca> registros);
}