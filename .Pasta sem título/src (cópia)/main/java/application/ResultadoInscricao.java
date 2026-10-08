package application;

import domain.model.Inscricao;

import java.util.Collections;
import java.util.List;

/** DTO de saída: a inscrição em si, mais os avisos que a política de conflito (S5) tenha gerado. */
public final class ResultadoInscricao {
    private final Inscricao inscricao;
    private final List<String> avisos;

    public ResultadoInscricao(Inscricao inscricao, List<String> avisos) {
        this.inscricao = inscricao;
        this.avisos = Collections.unmodifiableList(avisos);
    }

    public Inscricao getInscricao() {
        return inscricao;
    }

    public List<String> getAvisos() {
        return avisos;
    }
}
