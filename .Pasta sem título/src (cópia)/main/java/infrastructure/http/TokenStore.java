package infrastructure.http;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sessões em memória: token opaco -> id do usuário logado (RF-02).
 * Simplificação registrada: os tokens somem se o servidor reiniciar (não há persistência de
 * sessão). Para o núcleo obrigatório da disciplina isso não é exigido; fica como pendência caso
 * a equipe queira algo mais robusto (ex. JWT) numa próxima iteração.
 */
public class TokenStore {
    private final Map<String, UUID> tokens = new ConcurrentHashMap<>();

    public String emitir(UUID usuarioId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, usuarioId);
        return token;
    }

    public UUID resolver(String token) {
        if (token == null) return null;
        return tokens.get(token);
    }
}
