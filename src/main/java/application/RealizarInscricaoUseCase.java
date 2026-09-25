package application;

import domain.model.Atividade;
import domain.model.Inscricao;
import domain.ports.AtividadeRepository;
import domain.ports.InscricaoRepository;

import java.util.List;
import java.util.UUID;

public class RealizarInscricaoUseCase {

    private final InscricaoRepository inscricaoRepository;
    private final AtividadeRepository atividadeRepository;

    public RealizarInscricaoUseCase(InscricaoRepository inscricaoRepository, AtividadeRepository atividadeRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
    }

    public Inscricao executar(UUID participanteId, UUID atividadeId) {
        Atividade atividade = atividadeRepository.buscarPorId(atividadeId)
                .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada."));

        if (!atividade.temVagaDisponivel()) {
            throw new IllegalStateException("Não há vagas disponíveis para esta atividade.");
        }

        // Validação de conflito de horário na agenda
        List<Inscricao> inscricoesAtivas = inscricaoRepository.listarPorParticipante(participanteId);
        for (Inscricao inc : inscricoesAtivas) {
            if (inc.isAtiva()) {
                Atividade atvExistente = atividadeRepository.buscarPorId(inc.getAtividadeId()).orElse(null);
                if (atvExistente != null && atvExistente.getIntervalo().temConflitoCom(atividade.getIntervalo())) {
                    throw new IllegalStateException("Conflito de horário detectado com a atividade: " + atvExistente.getTitulo());
                }
            }
        }

        atividade.incrementarInscrito();
        Inscricao novaInscricao = new Inscricao(null, participanteId, atividadeId);
        
        atividadeRepository.salvar(atividade);
        inscricaoRepository.salvar(novaInscricao);

        return novaInscricao;
    }
}