package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record CambiarEstadoComando(UsuarioId actor, Long publicacionId, AccionPublicacion accion, String motivo) {
}
