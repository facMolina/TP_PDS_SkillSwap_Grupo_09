package ar.edu.uade.skillswap.aplicacion.puerto;

import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

public interface NotificacionRepositorio {

    Notificacion guardar(Notificacion notificacion);

    Optional<Notificacion> buscarPorId(Long id);

    Pagina<Notificacion> listarPorDestinatario(UsuarioId usuario, SolicitudPagina pagina);

    default Notificacion obtenerPorId(Long id) {
        return buscarPorId(id).orElseThrow(() -> new RecursoNoEncontrado("No existe la notificación " + id));
    }
}
