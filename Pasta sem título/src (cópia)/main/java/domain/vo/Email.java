package domain.vo;

import domain.exception.ValorInvalidoException;

import java.util.Locale;
import java.util.regex.Pattern;

/** Objeto de valor imutável. Normaliza (trim + minúsculas) para que RN-01 valha independentemente da caixa. */
public final class Email {
    private static final Pattern PADRAO =
            Pattern.compile("^[\\p{L}\\p{N}+_.-]+@([\\p{L}\\p{N}-]+\\.)+\\p{L}{2,}$");
    private static final int TAMANHO_MAXIMO = 254;

    private final String endereco;

    public Email(String endereco) {
        if (endereco == null || endereco.trim().isEmpty()) {
            throw new ValorInvalidoException("O e-mail é obrigatório.");
        }
        String normalizado = endereco.trim().toLowerCase(Locale.ROOT);
        if (normalizado.length() > TAMANHO_MAXIMO || !PADRAO.matcher(normalizado).matches()) {
            throw new ValorInvalidoException("Endereço de e-mail inválido.");
        }
        this.endereco = normalizado;
    }

    public String getEndereco() {
        return endereco;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return endereco.equals(((Email) o).endereco);
    }

    @Override
    public int hashCode() {
        return endereco.hashCode();
    }

    @Override
    public String toString() {
        return endereco;
    }
}
