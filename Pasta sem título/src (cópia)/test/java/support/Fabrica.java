package support;

import domain.model.Usuario;
import domain.vo.Email;
import domain.vo.IntervaloTempo;

import java.time.LocalDateTime;

/** Atalhos para montar objetos válidos nos testes. */
public final class Fabrica {
    private Fabrica() {}

    public static IntervaloTempo intervalo(int diasAPartirDeAmanha, int horaInicio, int horaFim) {
        LocalDateTime base = LocalDateTime.now().plusDays(diasAPartirDeAmanha + 1).withMinute(0).withSecond(0).withNano(0);
        return new IntervaloTempo(base.withHour(horaInicio), base.withHour(horaFim));
    }

    public static Usuario usuario(String email, Usuario.Perfil perfil) {
        return new Usuario(null, "Fulano de Tal", new Email(email), "hash", perfil);
    }
}
