package infrastructure.memoria;

import domain.model.avaliacao.Questionario;
import domain.ports.QuestionarioRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class QuestionarioRepositoryEmMemoria implements QuestionarioRepository {
    private final Map<UUID, Questionario> porId = new LinkedHashMap<>();

    @Override public void salvar(Questionario q) { porId.put(q.getId(), q); }

    @Override public Optional<Questionario> buscarPorId(UUID id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override public List<Questionario> listarPorAtividade(UUID atividadeId) {
        List<Questionario> resultado = new ArrayList<>();
        for (Questionario q : porId.values())
            if (q.getAtividadeId().equals(atividadeId)) resultado.add(q);
        return resultado;
    }
}
