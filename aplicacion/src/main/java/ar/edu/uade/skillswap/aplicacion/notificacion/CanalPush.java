package ar.edu.uade.skillswap.aplicacion.notificacion;

import ar.edu.uade.skillswap.aplicacion.puerto.EnviadorPush;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class CanalPush implements CanalNotificacion {

    private static final String TITULO = "SkillSwap";

    private final EnviadorPush enviador;

    public CanalPush(EnviadorPush enviador) {
        this.enviador = enviador;
    }

    @Override
    public void enviar(Notificacion notificacion, Perfil destino) {
        enviador.enviarPush(destino.getUsuarioId(), TITULO, notificacion.getMensaje());
    }
}
