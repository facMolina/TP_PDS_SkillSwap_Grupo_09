package ar.edu.uade.skillswap.dominio.comun;

import java.time.Instant;

public record ContextoAccion(UsuarioId actor, Instant ahora) {

    public static ContextoAccion de(UsuarioId actor, Instant ahora) {
        return new ContextoAccion(actor, ahora);
    }

    public static ContextoAccion delSistema(Instant ahora) {
        return new ContextoAccion(UsuarioId.SISTEMA, ahora);
    }
}
