package ar.edu.uade.skillswap.aplicacion.casouso.notificacion;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record MarcarLeidaComando(UsuarioId actor, Long notificacionId) {
}
