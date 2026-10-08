package domain.exception;

public class CredenciaisInvalidasException extends DominioException {
    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
