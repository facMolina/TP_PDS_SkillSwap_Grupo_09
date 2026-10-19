package ar.edu.uade.skillswap.infraestructura.externo;

import ar.edu.uade.skillswap.aplicacion.puerto.EnviadorPush;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public class AdaptadorPushSimulado implements EnviadorPush {

    private final ServicioPushSimulado servicio;

    public AdaptadorPushSimulado(ServicioPushSimulado servicio) {
        this.servicio = servicio;
    }

    @Override
    public void enviarPush(UsuarioId destinatario, String titulo, String cuerpo) {
        servicio.notificar("dispositivo-" + destinatario.valor(), titulo, cuerpo);
    }
}
