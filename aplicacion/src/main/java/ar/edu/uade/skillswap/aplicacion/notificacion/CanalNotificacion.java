package ar.edu.uade.skillswap.aplicacion.notificacion;

import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public interface CanalNotificacion {

    void enviar(Notificacion notificacion, Perfil destino);
}
