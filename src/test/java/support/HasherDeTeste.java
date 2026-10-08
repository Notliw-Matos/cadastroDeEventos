package support;

import domain.ports.PasswordHasher;

/** Hasher rápido e determinístico só para testes de caso de uso (o bcrypt real tem teste próprio). */
public class HasherDeTeste implements PasswordHasher {
    @Override
    public String hash(String senhaPura) {
        return "hash(" + new StringBuilder(senhaPura).reverse() + ")";
    }

    @Override
    public boolean confere(String senhaPura, String hash) {
        return senhaPura != null && hash(senhaPura).equals(hash);
    }
}
