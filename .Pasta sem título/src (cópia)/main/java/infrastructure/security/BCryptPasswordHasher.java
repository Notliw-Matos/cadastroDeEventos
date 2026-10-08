package infrastructure.security;

import domain.ports.PasswordHasher;
import org.mindrot.BCrypt;

/**
 * Adaptador de saída: bcrypt (RNF-05). Compatível com os hashes do seed, gerados no banco
 * por crypt(senha, gen_salt('bf')), então os usuários de demonstração conseguem logar.
 */
public class BCryptPasswordHasher implements PasswordHasher {

    private static final int CUSTO = 10;

    @Override
    public String hash(String senhaPura) {
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(CUSTO));
    }

    @Override
    public boolean confere(String senhaPura, String hash) {
        if (senhaPura == null || hash == null) return false;
        try {
            return BCrypt.checkpw(senhaPura, hash);
        } catch (IllegalArgumentException e) {
            return false; // hash em formato desconhecido nunca autentica
        }
    }
}
