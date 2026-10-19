package ar.edu.uade.skillswap.infraestructura.externo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class ServicioPushSimulado {

    public record PushEnviado(String dispositivo, String titulo, String texto) {
    }

    private static final Logger REGISTRO = Logger.getLogger(ServicioPushSimulado.class.getName());

    private final List<PushEnviado> enviados = new CopyOnWriteArrayList<>();

    public void notificar(String dispositivo, String titulo, String texto) {
        enviados.add(new PushEnviado(dispositivo, titulo, texto));
        REGISTRO.info(() -> "Push simulado a " + dispositivo + ": " + titulo);
    }

    public List<PushEnviado> enviados() {
        return List.copyOf(enviados);
    }
}
