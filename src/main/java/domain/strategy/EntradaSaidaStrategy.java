package domain.strategy;

import domain.model.RegistroPresenca;
import java.util.List;

public class EntradaSaidaStrategy implements ValidadorFrequenciaStrategy {
    @Override
    public boolean isPresencaValida(List<RegistroPresenca> registros) {
        if (registros == null || registros.size() < 2) return false;

        boolean temEntrada = registros.stream().anyMatch(r -> r.getTipo() == RegistroPresenca.TipoMarcacao.ENTRADA);
        boolean temSaida = registros.stream().anyMatch(r -> r.getTipo() == RegistroPresenca.TipoMarcacao.SAIDA);

        return temEntrada && temSaida;
    }
}