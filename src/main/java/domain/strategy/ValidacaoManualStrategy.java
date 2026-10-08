package domain.strategy;

import domain.model.RegistroPresenca;
import java.util.List;

/** Presença só vale se um usuário autorizado a lançou manualmente. */
public class ValidacaoManualStrategy implements ValidadorFrequenciaStrategy {
    @Override
    public boolean isPresencaValida(List<RegistroPresenca> registros) {
        if (registros == null) return false;
        return registros.stream().anyMatch(r -> r.getTipo() == RegistroPresenca.TipoMarcacao.MANUAL);
    }
}
