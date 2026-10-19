package ar.edu.uade.skillswap.infraestructura.persistencia.mapeo;

import ar.edu.uade.skillswap.dominio.calificacion.Calificacion;
import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.CalificacionEntidad;

public final class CalificacionMapeador {

    private CalificacionMapeador() {
    }

    public static CalificacionEntidad aEntidad(Calificacion c) {
        CalificacionEntidad e = new CalificacionEntidad();
        e.contratacionId = c.getContratacionId();
        e.autorId = c.getAutorId().valor();
        e.receptorId = c.getReceptorId().valor();
        e.puntaje = c.getPuntaje().valor();
        e.comentario = c.getComentario();
        e.fecha = c.getFecha();
        return e;
    }

    public static Calificacion aDominio(CalificacionEntidad e) {
        return Calificacion.reconstruir(e.id, e.contratacionId, new UsuarioId(e.autorId),
                new UsuarioId(e.receptorId), new Puntaje(e.puntaje), e.comentario, e.fecha);
    }
}
