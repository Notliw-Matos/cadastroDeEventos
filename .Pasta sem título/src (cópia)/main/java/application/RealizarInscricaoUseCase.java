package application;

import domain.exception.InscricaoDuplicadaException;
import domain.exception.RecursoNaoEncontradoException;
import domain.model.Atividade;
import domain.model.Inscricao;
import domain.ports.AtividadeRepository;
import domain.ports.InscricaoRepository;
import domain.strategy.PoliticaConflitoBloqueante;
import domain.strategy.PoliticaConflitoHorario;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RealizarInscricaoUseCase {

    private final InscricaoRepository inscricaoRepository;
    private final AtividadeRepository atividadeRepository;
    private final PoliticaConflitoHorario politicaConflito;

    public RealizarInscricaoUseCase(InscricaoRepository inscricaoRepository, AtividadeRepository atividadeRepository) {
        this(inscricaoRepository, atividadeRepository, new PoliticaConflitoBloqueante());
    }

    public RealizarInscricaoUseCase(InscricaoRepository inscricaoRepository, AtividadeRepository atividadeRepository,
                                    PoliticaConflitoHorario politicaConflito) {
        this.inscricaoRepository = inscricaoRepository;
        this.atividadeRepository = atividadeRepository;
        this.politicaConflito = politicaConflito;
    }

    public ResultadoInscricao executar(UUID participanteId, UUID atividadeId) {
        Atividade atividade = atividadeRepository.buscarPorId(atividadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada."));

        // RNF-11: repetir a operação por engano não pode duplicar a inscrição
        inscricaoRepository.buscarPorParticipanteEAtividade(participanteId, atividadeId)
                .filter(Inscricao::isAtiva)
                .ifPresent(existente -> { throw new InscricaoDuplicadaException(); });

        // RN-07 / RF-18: a atividade sabe dizer se conflita; a POLÍTICA decide o que fazer com isso
        // (bloquear ou só avisar) — o caso de uso não conhece qual das duas está em uso (ROO-05).
        List<String> avisos = new ArrayList<>();
        for (Inscricao ativa : inscricaoRepository.listarPorParticipante(participanteId)) {
            if (!ativa.isAtiva()) continue;
            atividadeRepository.buscarPorId(ativa.getAtividadeId())
                    .filter(atividade::conflitaCom)
                    .ifPresent(existente -> politicaConflito.verificar(atividade, existente, avisos));
        }

        atividade.incrementarInscrito();   // RN-06: lança VagasEsgotadasException se não houver vaga
        atividadeRepository.salvar(atividade); // persiste o novo total (ver nota em docs/S4_ENTREGA.md)

        Inscricao nova = new Inscricao(null, participanteId, atividadeId);
        inscricaoRepository.salvar(nova);
        return new ResultadoInscricao(nova, avisos);
    }
}
