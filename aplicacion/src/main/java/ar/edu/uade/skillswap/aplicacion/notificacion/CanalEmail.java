package ar.edu.uade.skillswap.aplicacion.notificacion;

import ar.edu.uade.skillswap.aplicacion.puerto.EnviadorEmail;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class CanalEmail implements CanalNotificacion {

    private static final String ASUNTO = "Notificación de SkillSwap";

    private final EnviadorEmail enviador;

    public CanalEmail(EnviadorEmail enviador) {
        this.enviador = enviador;
    }

    @Override
    public void enviar(Notificacion notificacion, Perfil destino) {
        enviador.enviarCorreo(destino.getEmail(), ASUNTO, notificacion.getMensaje());
    }
}
