package ar.edu.uade.skillswap.aplicacion.notificacion;

import ar.edu.uade.skillswap.aplicacion.puerto.NotificacionRepositorio;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class CanalInterno implements CanalNotificacion {

    private final NotificacionRepositorio repositorio;

    public CanalInterno(NotificacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void enviar(Notificacion notificacion, Perfil destino) {
        repositorio.guardar(notificacion);
    }
}
