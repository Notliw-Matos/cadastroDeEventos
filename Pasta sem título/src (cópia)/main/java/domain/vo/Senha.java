package domain.vo;

import domain.exception.ValorInvalidoException;

/**
 * Senha em texto puro que já passou pela política de senhas. Vive só o tempo de gerar o hash
 * e nunca é impressa (toString mascarado), para não vazar em logs (RNF-05, RNF-14).
 */
public final class Senha {
    public static final int MINIMO = 8;
    public static final int MAXIMO = 72; // limite do bcrypt

    private final String valor;

    private Senha(String valor) {
        this.valor = valor;
    }

    public static Senha de(String valor) {
        if (valor == null || valor.length() < MINIMO) {
            throw new ValorInvalidoException("A senha deve ter pelo menos " + MINIMO + " caracteres.");
        }
        if (valor.length() > MAXIMO) {
            throw new ValorInvalidoException("A senha deve ter no máximo " + MAXIMO + " caracteres.");
        }
        boolean temLetra = valor.chars().anyMatch(Character::isLetter);
        boolean temDigito = valor.chars().anyMatch(Character::isDigit);
        if (!temLetra || !temDigito) {
            throw new ValorInvalidoException("A senha deve conter letras e números.");
        }
        return new Senha(valor);
    }

    public String valor() {
        return valor;
    }

    @Override
    public String toString() {
        return "********";
    }
}
