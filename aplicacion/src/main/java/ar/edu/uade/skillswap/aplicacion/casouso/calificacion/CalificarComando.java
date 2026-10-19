package ar.edu.uade.skillswap.aplicacion.casouso.calificacion;

import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record CalificarComando(UsuarioId actor, Long contratacionId, Puntaje puntaje, String comentario) {
}
