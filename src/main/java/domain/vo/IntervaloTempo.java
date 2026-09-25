package domain.vo;

import java.time.LocalDateTime;
import java.util.Objects;

public final class IntervaloTempo {
    private final LocalDateTime inicio;
    private final LocalDateTime fim;

    public IntervaloTempo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("As datas do intervalo não podem ser nulas.");
        }
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data de término não pode ser anterior à data de início.");
        }
        this.inicio = inicio;
        this.fim = fim;
    }

    public boolean temConflitoCom(IntervaloTempo outro) {
        return this.inicio.isBefore(outro.fim) && outro.inicio.isBefore(this.fim);
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervaloTempo que = (IntervaloTempo) o;
        return Objects.equals(inicio, que.inicio) && Objects.equals(fim, que.fim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inicio, fim);
    }
}