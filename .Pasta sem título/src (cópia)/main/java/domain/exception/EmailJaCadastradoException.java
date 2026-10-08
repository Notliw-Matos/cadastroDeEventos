package domain.exception;

public class EmailJaCadastradoException extends DominioException {
    public EmailJaCadastradoException() {
        super("Este e-mail já está cadastrado.");
    }

    public EmailJaCadastradoException(String mensagem) {
        super(mensagem);
    }
}
