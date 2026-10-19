package ar.edu.uade.skillswap.infraestructura.externo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class ClienteCorreoSimulado {

    public record CorreoEnviado(String para, String asunto, String texto, boolean esHtml) {
    }

    private static final Logger REGISTRO = Logger.getLogger(ClienteCorreoSimulado.class.getName());

    private final List<CorreoEnviado> enviados = new CopyOnWriteArrayList<>();

    public void enviar(String para, String asunto, String texto, boolean esHtml) {
        enviados.add(new CorreoEnviado(para, asunto, texto, esHtml));
        REGISTRO.info(() -> "Correo simulado a " + para + ": " + asunto);
    }

    public List<CorreoEnviado> enviados() {
        return List.copyOf(enviados);
    }
}
