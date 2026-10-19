package ar.edu.uade.skillswap.aplicacion.casouso.notificacion;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.NotificacionRepositorio;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

public class MarcarNotificacionLeida {

    private final EjecutorTransaccional transaccion;
    private final NotificacionRepositorio notificaciones;

    public MarcarNotificacionLeida(EjecutorTransaccional transaccion, NotificacionRepositorio notificaciones) {
        this.transaccion = transaccion;
        this.notificaciones = notificaciones;
    }

    public void ejecutar(MarcarLeidaComando comando) {
        transaccion.ejecutar(() -> {
            Notificacion notificacion = notificaciones.obtenerPorId(comando.notificacionId());
            notificacion.marcarLeida(comando.actor());
            notificaciones.guardar(notificacion);
            return ResultadoTransaccion.sinEventos(null);
        });
    }
}
