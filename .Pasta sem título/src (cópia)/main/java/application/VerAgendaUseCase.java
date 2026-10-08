package application;

import application.ports.in.VerAgenda;
import domain.model.Atividade;
import domain.model.Inscricao;
import domain.ports.AtividadeRepository;
import domain.ports.InscricaoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class VerAgendaUseCase implements VerAgenda {

    private final InscricaoRepository inscricaoRepository;
    private final AtividadeRepository atividadeRepository;

    public VerAgendaUseCase(InscricaoRepository inscricaoRepository, AtividadeRepository atividadeRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
    }

    @Override
    public List<ItemAgendaDTO> executar(UUID participanteId) {
        // RN-08: a agenda deriva só das inscrições ATIVAS — uma cancelada não aparece mais.
        return inscricaoRepository.listarPorParticipante(participanteId).stream()
                .filter(Inscricao::isAtiva)
                .map(inscricao -> atividadeRepository.buscarPorId(inscricao.getAtividadeId())
                        .map(atividade -> ItemAgendaDTO.de(atividade, inscricao))
                        .orElse(null))
                .filter(item -> item != null)
                .sorted(Comparator.comparing(ItemAgendaDTO::getInicio)) // RF-17: ordem cronológica
                .collect(Collectors.toList());
    }
}
