package domain.model.avaliacao;

/** RF-25: três tipos obrigatórios. Cada constante sabe validar sua própria resposta. */
public enum TipoPergunta {
    TEXTO_LIVRE {
        @Override public void validarResposta(String valor) {
            if (valor == null || valor.trim().isEmpty())
                throw new domain.exception.ValorInvalidoException("Resposta de texto não pode ser vazia.");
        }
    },
    ESCOLHA_UNICA {
        @Override public void validarResposta(String valor) {
            if (valor == null || valor.trim().isEmpty())
                throw new domain.exception.ValorInvalidoException("Selecione uma opção.");
        }
    },
    ESCALA_NUMERICA {
        @Override public void validarResposta(String valor) {
            try {
                int n = Integer.parseInt(valor.trim());
                if (n < 1 || n > 10)
                    throw new domain.exception.ValorInvalidoException("Escala deve ser entre 1 e 10.");
            } catch (NumberFormatException e) {
                throw new domain.exception.ValorInvalidoException("Valor de escala deve ser um número inteiro.");
            }
        }
    };

    /** Lança {@link domain.exception.ValorInvalidoException} se o valor não for válido para este tipo. */
    public abstract void validarResposta(String valor);
}
