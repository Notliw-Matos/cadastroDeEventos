package domain.ports;

import domain.model.RegistroPresenca;

import java.util.List;
import java.util.UUID;

public interface RegistroFrequenciaRepository {
    void salvar(RegistroPresenca registro);
    List<RegistroPresenca> listarPorInscricao(UUID inscricaoId);
}
