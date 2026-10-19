package ar.edu.uade.skillswap.aplicacion.casouso.notificacion;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.puerto.NotificacionRepositorio;

public class ListarNotificaciones {

    private final EjecutorTransaccional transaccion;
    private final NotificacionRepositorio notificaciones;

    public ListarNotificaciones(EjecutorTransaccional transaccion, NotificacionRepositorio notificaciones) {
        this.transaccion = transaccion;
        this.notificaciones = notificaciones;
    }

    public Pagina<NotificacionVista> ejecutar(ListadoNotificaciones listado) {
        return transaccion.consultar(() -> notificaciones
                .listarPorDestinatario(listado.actor(), listado.pagina())
                .transformar(NotificacionVista::de));
    }
}
