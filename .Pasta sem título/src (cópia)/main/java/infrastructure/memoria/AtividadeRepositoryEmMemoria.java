package infrastructure.memoria;

import domain.model.Atividade;
import domain.ports.AtividadeRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AtividadeRepositoryEmMemoria implements AtividadeRepository {
    private final Map<UUID, Atividade> porId = new LinkedHashMap<>();

    @Override
    public void salvar(Atividade atividade) {
        porId.put(atividade.getId(), atividade);
    }

    @Override
    public Optional<Atividade> buscarPorId(UUID id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<Atividade> listarPorEvento(UUID eventoId) {
        List<Atividade> resultado = new ArrayList<>();
        for (Atividade a : porId.values()) {
            if (a.getEventoId().equals(eventoId)) resultado.add(a);
        }
        resultado.sort((a, b) -> a.getIntervalo().getInicio().compareTo(b.getIntervalo().getInicio()));
        return resultado;
    }
}
