package domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    private final String endereco;

    public Email(String endereco) {
        if (endereco == null || !PATTERN.matcher(endereco).matches()) {
            throw new IllegalArgumentException("Endereço de e-mail inválido.");
        }
        this.endereco = endereco;
    }

    public String getEndereco() {
        return endereco;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(endereco, email.endereco);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endereco);
    }
}