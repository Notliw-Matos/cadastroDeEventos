package application.ports.in;

import application.UsuarioResumo;

/** Porta de entrada (RF-01): cadastro público de participante. */
public interface CadastrarUsuario {
    UsuarioResumo executar(String nome, String email, String senha);
}
