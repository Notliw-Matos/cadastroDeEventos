package application.ports.in;

import application.UsuarioResumo;

/** Porta de entrada (RF-02): valida credenciais e devolve quem é o usuário. */
public interface AutenticarUsuario {
    UsuarioResumo executar(String email, String senha);
}
