package domain.exception;

public class InscricaoDuplicadaException extends DominioException {
    public InscricaoDuplicadaException() {
        super("Participante já inscrito nesta atividade.");
    }

    public InscricaoDuplicadaException(String mensagem) {
        super(mensagem);
    }
}
