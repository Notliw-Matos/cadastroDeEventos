package infrastructure.http;

/** Erro de protocolo HTTP (rota inexistente, corpo ilegível, autenticação ausente) — não é regra de domínio. */
public class RequisicaoInvalidaException extends RuntimeException {
    private final int codigoHttp;

    public RequisicaoInvalidaException(int codigoHttp, String mensagem) {
        super(mensagem);
        this.codigoHttp = codigoHttp;
    }

    public int getCodigoHttp() {
        return codigoHttp;
    }
}
