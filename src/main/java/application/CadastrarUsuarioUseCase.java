package application;

import application.ports.in.CadastrarUsuario;
import domain.exception.EmailJaCadastradoException;
import domain.model.Usuario;
import domain.ports.PasswordHasher;
import domain.ports.UsuarioRepository;
import domain.vo.Email;
import domain.vo.Senha;

public class CadastrarUsuarioUseCase implements CadastrarUsuario {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public CadastrarUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UsuarioResumo executar(String nome, String emailInformado, String senhaInformada) {
        Email email = new Email(emailInformado);      // valida formato
        Senha senha = Senha.de(senhaInformada);       // valida política de senha

        if (usuarioRepository.existePorEmail(email)) {
            throw new EmailJaCadastradoException();   // RN-01
        }

        Usuario usuario = Usuario.novoParticipante(nome, email, passwordHasher.hash(senha.valor()));
        usuarioRepository.salvar(usuario);
        return UsuarioResumo.de(usuario);
    }
}
