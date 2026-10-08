package application;

import domain.model.Usuario;

import java.util.UUID;

/** DTO de saída: dados públicos do usuário. Deliberadamente NÃO carrega o hash da senha. */
public final class UsuarioResumo {
    private final UUID id;
    private final String nome;
    private final String email;
    private final Usuario.Perfil perfil;

    private UsuarioResumo(UUID id, String nome, String email, Usuario.Perfil perfil) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
    }

    public static UsuarioResumo de(Usuario usuario) {
        return new UsuarioResumo(usuario.getId(), usuario.getNome(),
                usuario.getEmail().getEndereco(), usuario.getPerfil());
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Usuario.Perfil getPerfil() { return perfil; }
}
