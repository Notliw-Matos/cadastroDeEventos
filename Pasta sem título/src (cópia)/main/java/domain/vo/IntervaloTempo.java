package domain.vo;

import domain.exception.ValorInvalidoException;

import java.time.LocalDateTime;
import java.util.Objects;

/** Período de tempo válido por construção: início e fim existem e o fim vem depois do início. */
public final class IntervaloTempo {
    private final LocalDateTime inicio;
    private final LocalDateTime fim;

    public IntervaloTempo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new ValorInvalidoException("As datas do intervalo não podem ser nulas.");
        }
        if (!fim.isAfter(inicio)) {
            throw new ValorInvalidoException("A data de término deve ser posterior à data de início.");
        }
        this.inicio = inicio;
        this.fim = fim;
    }

    /** Dois intervalos conflitam se se sobrepõem; terminar exatamente quando o outro começa NÃO é conflito. */
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
        return inicio.equals(que.inicio) && fim.equals(que.fim);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inicio, fim);
    }

    @Override
    public String toString() {
        return inicio + " ate " + fim;
    }
}
