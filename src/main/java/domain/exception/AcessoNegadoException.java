package domain.exception;

public class AcessoNegadoException extends DominioException {
    public AcessoNegadoException() {
        super("Você não tem permissão para realizar esta operação.");
    }

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
