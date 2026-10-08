package application;

import application.ports.in.AutenticarUsuario;
import domain.exception.CredenciaisInvalidasException;
import domain.exception.ValorInvalidoException;
import domain.model.Usuario;
import domain.ports.PasswordHasher;
import domain.ports.UsuarioRepository;
import domain.vo.Email;

public class AutenticarUsuarioUseCase implements AutenticarUsuario {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UsuarioResumo executar(String emailInformado, String senhaInformada) {
        // Mesma mensagem para e-mail inexistente e senha errada: não revela quais e-mails existem.
        Email email;
        try {
            email = new Email(emailInformado);
        } catch (ValorInvalidoException e) {
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordHasher.confere(senhaInformada, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        return UsuarioResumo.de(usuario);
    }
}
