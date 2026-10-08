package domain.ports;

import domain.model.Usuario;
import domain.vo.Email;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    /** @throws domain.exception.EmailJaCadastradoException se outro usuário já usa o e-mail (RN-01). */
    void salvar(Usuario usuario);
    Optional<Usuario> buscarPorId(UUID id);
    Optional<Usuario> buscarPorEmail(Email email);
    boolean existePorEmail(Email email);
}
