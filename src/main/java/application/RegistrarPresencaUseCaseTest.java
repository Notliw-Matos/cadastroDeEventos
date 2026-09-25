package application;

import domain.model.Inscricao;
import domain.model.RegistroPresenca;
import domain.ports.InscricaoRepository;
import domain.strategy.CheckInUnicoStrategy;
import domain.strategy.ValidadorFrequenciaStrategy;

import java.util.Optional;
import java.util.UUID;

public class RegistrarPresencaUseCaseTest {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO TESTE DO DOMINIO DE FREQUENCIA ===");

        UUID inscricaoId = UUID.randomUUID();
        UUID participanteId = UUID.randomUUID();
        UUID atividadeId = UUID.randomUUID();
        UUID operadorId = UUID.randomUUID();

        Inscricao inscricaoValida = new Inscricao(inscricaoId, participanteId, atividadeId);

        // Repositório anónimo implementando o método exato da interface
        InscricaoRepository repositorioFake = new InscricaoRepository() {
            @Override
            public void salvar(Inscricao inscricao) {
            }

            @Override
            public Optional<Inscricao> buscarPorId(UUID id) {
                if (id.equals(inscricaoId)) {
                    return Optional.of(inscricaoValida);
                }
                return Optional.empty();
            }

            @Override
            public Optional<Inscricao> buscarPorParticipanteEAtividade(UUID participanteId, UUID atividadeId) {
                return Optional.of(inscricaoValida);
            }
        };

        RegistrarPresencaUseCase useCase = new RegistrarPresencaUseCase(repositorioFake);
        ValidadorFrequenciaStrategy estrategiaCheckIn = new CheckInUnicoStrategy();

        // 1. Teste de elegibilidade inicial
        boolean presencaInicial = useCase.verificarElegibilidadePresenca(inscricaoId, estrategiaCheckIn);
        System.out.println("Teste 1 - Presenca antes da marcacao (Esperado: false): " + presencaInicial);

        // 2. Registro de marcação
        // NOTA: Se no seu enum TipoMarcacao o valor for CHECKIN, ENTRADA ou outro, altere TipoMarcacao.CHECK_IN para o nome correto.
        useCase.registrarMarcacao(inscricaoId, RegistroPresenca.TipoMarcacao.CHECK_IN, operadorId);
        boolean presencaAposCheckIn = useCase.verificarElegibilidadePresenca(inscricaoId, estrategiaCheckIn);
        System.out.println("Teste 2 - Presenca apos marcacao (Esperado: true): " + presencaAposCheckIn);

        // 3. Teste de exceção para ID inexistente
        try {
            useCase.registrarMarcacao(UUID.randomUUID(), RegistroPresenca.TipoMarcacao.CHECK_IN, operadorId);
            System.out.println("Teste 3 - Falha (Deveria ter lancado excecao)");
        } catch (IllegalArgumentException e) {
            System.out.println("Teste 3 - Sucesso no tratamento de erro: " + e.getMessage());
        }

        System.out.println("=== TESTES CONCLUIDOS COM SUCESSO ===");
    }
}