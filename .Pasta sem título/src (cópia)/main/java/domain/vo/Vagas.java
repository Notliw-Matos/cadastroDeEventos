package domain.vo;

import domain.exception.ValorInvalidoException;

/** Política de capacidade (RF-14): ilimitada (sem controle) ou limitada a N vagas. */
public final class Vagas {
    private static final Vagas ILIMITADAS = new Vagas(null);

    private final Integer limite; // null = sem controle de vagas

    private Vagas(Integer limite) {
        this.limite = limite;
    }

    public static Vagas ilimitadas() {
        return ILIMITADAS;
    }

    public static Vagas limitadas(int limite) {
        if (limite <= 0) {
            throw new ValorInvalidoException("O número de vagas deve ser maior que zero.");
        }
        return new Vagas(limite);
    }

    /** Conveniência para a persistência: coluna nula significa sem controle. */
    public static Vagas doLimite(Integer limiteOuNulo) {
        return limiteOuNulo == null ? ILIMITADAS : limitadas(limiteOuNulo);
    }

    /** Ainda cabe mais um, dado o número de ocupadas? */
    public boolean haVagaCom(int ocupadas) {
        return limite == null || ocupadas < limite;
    }

    /** O total informado respeita o limite? (usado para validar dados reconstituídos) */
    public boolean comporta(int total) {
        return limite == null || total <= limite;
    }

    public boolean isLimitada() {
        return limite != null;
    }

    /** Valor para persistir; nulo quando ilimitada. */
    public Integer getLimite() {
        return limite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vagas outras = (Vagas) o;
        return limite == null ? outras.limite == null : limite.equals(outras.limite);
    }

    @Override
    public int hashCode() {
        return limite == null ? 0 : limite.hashCode();
    }

    @Override
    public String toString() {
        return limite == null ? "sem limite" : limite + " vagas";
    }
}
