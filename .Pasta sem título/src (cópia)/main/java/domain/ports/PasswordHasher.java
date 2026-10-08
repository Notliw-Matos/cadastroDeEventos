package domain.ports;

/** Porta de saída: o domínio exige hash seguro (RNF-05) sem saber qual algoritmo o implementa. */
public interface PasswordHasher {
    String hash(String senhaPura);
    boolean confere(String senhaPura, String hash);
}
