package ar.edu.uade.skillswap.dominio.evento;

import java.time.Instant;
import java.util.List;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record EventoContratacion(TipoEvento tipo, Long contratacionId, UsuarioId actorId,
                                 List<UsuarioId> destinatarios, Instant ocurridoEn) {
}
