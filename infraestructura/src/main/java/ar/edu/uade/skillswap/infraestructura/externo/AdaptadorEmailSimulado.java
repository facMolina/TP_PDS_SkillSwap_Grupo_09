package ar.edu.uade.skillswap.infraestructura.externo;

import ar.edu.uade.skillswap.aplicacion.puerto.EnviadorEmail;

public class AdaptadorEmailSimulado implements EnviadorEmail {

    private final ClienteCorreoSimulado cliente;

    public AdaptadorEmailSimulado(ClienteCorreoSimulado cliente) {
        this.cliente = cliente;
    }

    @Override
    public void enviarCorreo(String destino, String asunto, String cuerpo) {
        cliente.enviar(destino, asunto, cuerpo, false);
    }
}
