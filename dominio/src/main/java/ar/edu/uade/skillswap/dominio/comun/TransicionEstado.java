package ar.edu.uade.skillswap.dominio.comun;

import java.time.Instant;

public record TransicionEstado(Instant fecha, UsuarioId actorId, String origen, String destino, String motivo) {
}
