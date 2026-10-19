package ar.edu.uade.skillswap.aplicacion.casouso.notificacion;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

public record NotificacionVista(Long id, TipoEvento tipo, String mensaje, Instant fecha, boolean leida) {

    public static NotificacionVista de(Notificacion notificacion) {
        return new NotificacionVista(notificacion.getId(), notificacion.getTipo(), notificacion.getMensaje(),
                notificacion.getFecha(), notificacion.estaLeida());
    }
}
