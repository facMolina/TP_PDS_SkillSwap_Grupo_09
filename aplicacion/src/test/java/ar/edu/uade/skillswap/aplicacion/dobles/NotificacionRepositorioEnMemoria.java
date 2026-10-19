package ar.edu.uade.skillswap.aplicacion.dobles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.puerto.NotificacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

public class NotificacionRepositorioEnMemoria implements NotificacionRepositorio {

    private final List<Notificacion> almacen = new ArrayList<>();
    private long secuencia;

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        if (notificacion.getId() == null) {
            notificacion.asignarId(++secuencia);
            almacen.add(notificacion);
        }
        return notificacion;
    }

    @Override
    public Optional<Notificacion> buscarPorId(Long id) {
        return almacen.stream().filter(notificacion -> notificacion.getId().equals(id)).findFirst();
    }

    @Override
    public Pagina<Notificacion> listarPorDestinatario(UsuarioId usuario, SolicitudPagina pagina) {
        return Pagina.de(almacen.stream().filter(notificacion -> notificacion.perteneceA(usuario)).toList(), pagina);
    }
}
