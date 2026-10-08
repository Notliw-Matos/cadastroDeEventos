package domain.exception;

public class VagasEsgotadasException extends DominioException {
    public VagasEsgotadasException() {
        super("Não há vagas disponíveis para esta atividade.");
    }

    public VagasEsgotadasException(String mensagem) {
        super(mensagem);
    }
}
