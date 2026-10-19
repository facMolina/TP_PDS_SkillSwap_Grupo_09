package ar.edu.uade.skillswap.aplicacion.casouso.calificacion;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.calificacion.Calificacion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record CalificacionVista(Long id, Long contratacionId, UsuarioId autor, UsuarioId receptor, int puntaje,
                                String comentario, Instant fecha) {

    public static CalificacionVista de(Calificacion calificacion) {
        return new CalificacionVista(calificacion.getId(), calificacion.getContratacionId(),
                calificacion.getAutorId(), calificacion.getReceptorId(), calificacion.getPuntaje().valor(),
                calificacion.getComentario(), calificacion.getFecha());
    }
}
