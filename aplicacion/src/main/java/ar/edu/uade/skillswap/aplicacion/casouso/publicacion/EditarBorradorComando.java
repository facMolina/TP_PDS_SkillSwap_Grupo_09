package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record EditarBorradorComando(UsuarioId actor, Long publicacionId, DatosPublicacionComando datos) {
}
