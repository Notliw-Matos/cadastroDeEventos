package infrastructure.memoria;

import domain.model.Inscricao;
import domain.ports.InscricaoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InscricaoRepositoryEmMemoria implements InscricaoRepository {
    private final Map<UUID, Inscricao> porId = new LinkedHashMap<>();

    @Override
    public void salvar(Inscricao inscricao) {
        porId.put(inscricao.getId(), inscricao);
    }

    @Override
    public Optional<Inscricao> buscarPorId(UUID id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public Optional<Inscricao> buscarPorParticipanteEAtividade(UUID participanteId, UUID atividadeId) {
        return porId.values().stream()
                .filter(i -> i.getParticipanteId().equals(participanteId) && i.getAtividadeId().equals(atividadeId))
                .findFirst();
    }

    @Override
    public List<Inscricao> listarPorParticipante(UUID participanteId) {
        List<Inscricao> resultado = new ArrayList<>();
        for (Inscricao i : porId.values()) {
            if (i.getParticipanteId().equals(participanteId)) resultado.add(i);
        }
        return resultado;
    }
    @Override
    public List<Inscricao> listarPorAtividade(UUID atividadeId) {
        List<Inscricao> resultado = new ArrayList<>();
        for (Inscricao i : porId.values()) {
            if (i.getAtividadeId().equals(atividadeId)) resultado.add(i);
        }
        return resultado;
    }
}
