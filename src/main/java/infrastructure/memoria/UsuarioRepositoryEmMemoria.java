package infrastructure.memoria;

import domain.exception.EmailJaCadastradoException;
import domain.model.Usuario;
import domain.ports.UsuarioRepository;
import domain.vo.Email;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de saída em memória: implementa a MESMA porta {@link UsuarioRepository} que o
 * adaptador de banco. Serve para os testes de caso de uso, para a demonstração sem depender
 * do Supabase, e é o adaptador padrão do modo "sem banco" da API (ver app.ServidorApi).
 */
public class UsuarioRepositoryEmMemoria implements UsuarioRepository {
    private final Map<UUID, Usuario> porId = new LinkedHashMap<>();

    @Override
    public void salvar(Usuario usuario) {
        boolean emailDeOutro = porId.values().stream()
                .anyMatch(u -> u.getEmail().equals(usuario.getEmail()) && !u.getId().equals(usuario.getId()));
        if (emailDeOutro) throw new EmailJaCadastradoException();
        porId.put(usuario.getId(), usuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
        return porId.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    @Override
    public boolean existePorEmail(Email email) {
        return buscarPorEmail(email).isPresent();
    }

    public int total() {
        return porId.size();
    }
}
