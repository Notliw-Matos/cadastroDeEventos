package domain.model;

import domain.exception.AcessoNegadoException;
import domain.exception.ValorInvalidoException;
import domain.vo.Email;

import java.util.UUID;

public class Usuario {

    /** Cada perfil sabe o que pode fazer: permissões são dados do perfil, não if espalhado (RNF-06). */
    public enum Perfil {
        ADMINISTRADOR(true, true),
        ORGANIZADOR(true, false),
        PARTICIPANTE(false, false);

        private final boolean gerenciaEventos;
        private final boolean administraTudo;

        Perfil(boolean gerenciaEventos, boolean administraTudo) {
            this.gerenciaEventos = gerenciaEventos;
            this.administraTudo = administraTudo;
        }

        public boolean podeGerenciarEventos() {
            return gerenciaEventos;
        }

        public boolean isAdministrador() {
            return administraTudo;
        }
    }

    private final UUID id;
    private final String nome;
    private final Email email;
    private final String senhaHash;
    private final Perfil perfil;

    public Usuario(UUID id, String nome, Email email, String senhaHash, Perfil perfil) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValorInvalidoException("Nome é obrigatório.");
        }
        if (email == null) {
            throw new ValorInvalidoException("E-mail é obrigatório.");
        }
        if (senhaHash == null || senhaHash.isEmpty()) {
            throw new ValorInvalidoException("Senha é obrigatória.");
        }
        if (perfil == null) {
            throw new ValorInvalidoException("Perfil é obrigatório.");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.nome = nome.trim();
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    /** Todo cadastro público nasce como participante: perfil elevado nunca vem do formulário. */
    public static Usuario novoParticipante(String nome, Email email, String senhaHash) {
        return new Usuario(null, nome, email, senhaHash, Perfil.PARTICIPANTE);
    }

    public boolean podeGerenciarEventos() {
        return perfil.podeGerenciarEventos();
    }

    public boolean isAdministrador() {
        return perfil.isAdministrador();
    }

    public void exigirPermissaoParaGerenciarEventos() {
        if (!podeGerenciarEventos()) {
            throw new AcessoNegadoException("Apenas organizadores e administradores podem gerenciar eventos.");
        }
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public Email getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public Perfil getPerfil() { return perfil; }
}
