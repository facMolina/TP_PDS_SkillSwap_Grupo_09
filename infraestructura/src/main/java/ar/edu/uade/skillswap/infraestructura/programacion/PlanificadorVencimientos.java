package ar.edu.uade.skillswap.infraestructura.programacion;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.VencerContratacionesSinPago;

public class PlanificadorVencimientos implements AutoCloseable {

    private static final Logger REGISTRO = Logger.getLogger(PlanificadorVencimientos.class.getName());

    private final VencerContratacionesSinPago caso;
    private final Duration intervalo;
    private ScheduledExecutorService ejecutor;

    public PlanificadorVencimientos(VencerContratacionesSinPago caso, Duration intervalo) {
        this.caso = caso;
        this.intervalo = intervalo;
    }

    public synchronized void iniciar() {
        if (ejecutor != null) {
            return;
        }
        ejecutor = Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "planificador-vencimientos");
            hilo.setDaemon(true);
            return hilo;
        });
        ejecutor.scheduleWithFixedDelay(this::ejecutarCiclo, intervalo.toMillis(), intervalo.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    public void ejecutarCiclo() {
        try {
            caso.ejecutar();
        } catch (RuntimeException error) {
            REGISTRO.log(Level.WARNING, "Falló el ciclo de vencimientos", error);
        }
    }

    @Override
    public synchronized void close() {
        if (ejecutor != null) {
            ejecutor.shutdownNow();
            ejecutor = null;
        }
    }
}
