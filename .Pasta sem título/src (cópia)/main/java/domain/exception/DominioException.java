package domain.exception;

/** Base de todas as falhas de regra de negócio. Mensagens são pensadas para o usuário final. */
public class DominioException extends RuntimeException {
    public DominioException(String mensagem) {
        super(mensagem);
    }
}
