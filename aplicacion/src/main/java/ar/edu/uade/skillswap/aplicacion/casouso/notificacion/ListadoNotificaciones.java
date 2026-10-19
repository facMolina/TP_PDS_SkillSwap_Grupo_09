package ar.edu.uade.skillswap.aplicacion.casouso.notificacion;

import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record ListadoNotificaciones(UsuarioId actor, SolicitudPagina pagina) {

    public ListadoNotificaciones {
        pagina = pagina == null ? SolicitudPagina.primera() : pagina;
    }
}
