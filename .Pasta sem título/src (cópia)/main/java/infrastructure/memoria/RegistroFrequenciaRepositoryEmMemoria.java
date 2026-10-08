package infrastructure.memoria;

import domain.model.RegistroPresenca;
import domain.ports.RegistroFrequenciaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegistroFrequenciaRepositoryEmMemoria implements RegistroFrequenciaRepository {
    private final List<RegistroPresenca> registros = new ArrayList<>();

    @Override
    public void salvar(RegistroPresenca registro) {
        registros.add(registro);
    }

    @Override
    public List<RegistroPresenca> listarPorInscricao(UUID inscricaoId) {
        List<RegistroPresenca> resultado = new ArrayList<>();
        for (RegistroPresenca r : registros) {
            if (r.getInscricaoId().equals(inscricaoId)) resultado.add(r);
        }
        return resultado;
    }
}
