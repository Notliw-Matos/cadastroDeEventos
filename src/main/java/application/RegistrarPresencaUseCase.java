package application;

import domain.model.Inscricao;
import domain.model.RegistroPresenca;
import domain.ports.InscricaoRepository;
import domain.strategy.ValidadorFrequenciaStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RegistrarPresencaUseCase {

    private final InscricaoRepository inscricaoRepository;
    private final List<RegistroPresenca> registrosMemoria = new ArrayList<>();

    public RegistrarPresencaUseCase(InscricaoRepository inscricaoRepository) {
        this.inscricaoRepository = inscricaoRepository;
    }

    public void registrarMarcacao(UUID inscricaoId, RegistroPresenca.TipoMarcacao tipo, UUID operadoPor) {
        Inscricao inscricao = inscricaoRepository.buscarPorId(inscricaoId)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        if (!inscricao.isAtiva()) {
            throw new IllegalStateException("Não é possível registrar presença para uma inscrição cancelada.");
        }

        RegistroPresenca novoRegistro = new RegistroPresenca(null, inscricaoId, tipo, operadoPor);
        registrosMemoria.add(novoRegistro);
    }

    public boolean verificarElegibilidadePresenca(UUID inscricaoId, ValidadorFrequenciaStrategy estrategia) {
        List<RegistroPresenca> doParticipante = registrosMemoria.stream()
                .filter(r -> r.getInscricaoId().equals(inscricaoId))
                .collect(Collectors.toList());

        return estrategia.isPresencaValida(doParticipante);
    }
}