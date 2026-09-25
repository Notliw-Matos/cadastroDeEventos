package domain;

import domain.model.Atividade;
import domain.vo.IntervaloTempo;

import java.time.LocalDateTime;
import java.util.UUID;

public class AtividadeTest {

    public static void main(String[] args) {
        System.out.println("--- EXECUTANDO TESTES DE DOMÍNIO ---");

        // 1. Teste de Capacidade Limite
        IntervaloTempo intervalo = new IntervaloTempo(
                LocalDateTime.now(), 
                LocalDateTime.now().plusHours(2)
        );
        Atividade atividade = new Atividade(
                UUID.randomUUID(), UUID.randomUUID(), 
                "Oficina POO", Atividade.TipoAtividade.OFICINA, 
                intervalo, "Lab 01", 1
        );

        atividade.incrementarInscrito(); // 1/1 preenchido
        System.out.println("Inscrição 1 realizada com sucesso!");

        try {
            atividade.incrementarInscrito(); // Deve lançar exceção!
            System.err.println("ERRO: O sistema permitiu ultrapassar a vaga!");
        } catch (IllegalStateException e) {
            System.out.println("SUCESSO: Invariante protegida! " + e.getMessage());
        }
    }
}