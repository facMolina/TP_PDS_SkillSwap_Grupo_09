package ar.edu.uade.skillswap.infraestructura.programacion;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.VencerContratacionesSinPago;

class PlanificadorVencimientosTest {

    @Test
    void elCicloManualEjecutaElCasoDeUso() {
        VencerContratacionesSinPago caso = mock(VencerContratacionesSinPago.class);

        new PlanificadorVencimientos(caso, Duration.ofMinutes(5)).ejecutarCiclo();

        verify(caso).ejecutar();
    }

    @Test
    void unaFallaEnElCicloNoDetieneAlPlanificador() {
        VencerContratacionesSinPago caso = mock(VencerContratacionesSinPago.class);
        doThrow(new IllegalStateException("falla")).when(caso).ejecutar();

        new PlanificadorVencimientos(caso, Duration.ofMinutes(5)).ejecutarCiclo();

        verify(caso).ejecutar();
    }

    @Test
    void elPlanificadorEjecutaElCasoDeFormaPeriodicaHastaCerrarse() throws InterruptedException {
        VencerContratacionesSinPago caso = mock(VencerContratacionesSinPago.class);
        try (PlanificadorVencimientos planificador = new PlanificadorVencimientos(caso, Duration.ofMillis(20))) {
            planificador.iniciar();
            planificador.iniciar();
            Thread.sleep(200);
        }

        verify(caso, atLeastOnce()).ejecutar();
    }
}
