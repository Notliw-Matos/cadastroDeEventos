package infrastructure.controllers;

import application.RealizarInscricaoUseCase;
import domain.model.Inscricao;

import java.util.UUID;

// Exemplo de adaptador REST (Driver Adapter)
public class InscricaoController {

    private final RealizarInscricaoUseCase realizarInscricaoUseCase;

    public InscricaoController(RealizarInscricaoUseCase realizarInscricaoUseCase) {
        this.realizarInscricaoUseCase = realizarInscricaoUseCase;
    }

    // Endpoint POST /api/inscricoes
    public String inscreverParticipante(String participanteIdStr, String atividadeIdStr) {
        try {
            UUID participanteId = UUID.fromString(participanteIdStr);
            UUID atividadeId = UUID.fromString(atividadeIdStr);

            Inscricao inscricao = realizarInscricaoUseCase.executar(participanteId, atividadeId);
            return "{\"status\": \"sucesso\", \"inscricaoId\": \"" + inscricao.getId() + "\"}";
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "{\"status\": \"erro\", \"mensagem\": \"" + e.getMessage() + "\"}";
        }
    }
}