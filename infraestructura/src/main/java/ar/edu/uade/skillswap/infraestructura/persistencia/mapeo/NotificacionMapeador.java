package ar.edu.uade.skillswap.infraestructura.persistencia.mapeo;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.NotificacionEntidad;

public final class NotificacionMapeador {

    private NotificacionMapeador() {
    }

    public static NotificacionEntidad nueva(Notificacion n) {
        NotificacionEntidad e = new NotificacionEntidad();
        e.destinatarioId = n.getDestinatarioId().valor();
        e.tipo = n.getTipo().name();
        e.mensaje = n.getMensaje();
        e.fecha = n.getFecha();
        return e;
    }

    public static void copiar(Notificacion n, NotificacionEntidad e) {
        e.leida = n.estaLeida();
    }

    public static Notificacion aDominio(NotificacionEntidad e) {
        return Notificacion.reconstruir(e.id, new UsuarioId(e.destinatarioId), TipoEvento.valueOf(e.tipo), e.mensaje,
                e.fecha, e.leida);
    }
}
