package domain.model;

import domain.vo.Email;
import java.util.UUID;

public class Usuario {
    public enum Perfil { ADMINISTRADOR, ORGANIZADOR, PARTICIPANTE }

    private final UUID id;
    private String nome;
    private final Email email;
    private String senhaHash;
    private final Perfil perfil;

    public Usuario(UUID id, String nome, Email email, String senhaHash, Perfil perfil) {
        if (nome == null || nome.isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (senhaHash == null || senhaHash.isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória.");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = perfil != null ? perfil : Perfil.PARTICIPANTE;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public Email getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public Perfil getPerfil() { return perfil; }
}