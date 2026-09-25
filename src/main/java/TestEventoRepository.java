import domain.model.Evento;
import domain.ports.EventoRepository;
import domain.vo.IntervaloTempo;
import infrastructure.persistence.EventoRepositoryDatabase;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class TestEventoRepository {

    public static void main(String[] args) {
        EventoRepository repository = new EventoRepositoryDatabase();

        // 1. Cria um evento de teste
        UUID id = UUID.randomUUID();
        IntervaloTempo periodo = new IntervaloTempo(
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2)
        );
        Evento evento = new Evento(
                id,
                "Evento de Teste",
                "Descricao de teste gerada pelo TestEventoRepository",
                periodo,
                "Auditorio Principal"
        );

        // 2. Tenta salvar
        System.out.println("Salvando evento com id: " + id);
        try {
            repository.salvar(evento);
            System.out.println("Evento salvo com sucesso!");
        } catch (Exception e) {
            System.out.println("ERRO ao salvar evento: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 3. Tenta buscar de volta pelo mesmo id
        System.out.println("Buscando evento pelo id: " + id);
        Optional<Evento> encontrado = repository.buscarPorId(id);

        if (encontrado.isPresent()) {
            Evento e = encontrado.get();
            System.out.println("Evento encontrado!");
            System.out.println("  Titulo: " + e.getTitulo());
            System.out.println("  Descricao: " + e.getDescricao());
            System.out.println("  Local: " + e.getLocal());
            System.out.println("  Inicio: " + e.getPeriodo().getInicio());
            System.out.println("  Fim: " + e.getPeriodo().getFim());
        } else {
            System.out.println("ERRO: evento nao foi encontrado apos salvar. Verifique a tabela 'eventos' no banco.");
        }
    }
}
