package ar.edu.uade.skillswap.aplicacion.casouso;

import static ar.edu.uade.skillswap.aplicacion.Escenario.CLIENTE;
import static ar.edu.uade.skillswap.aplicacion.Escenario.F1;
import static ar.edu.uade.skillswap.aplicacion.Escenario.PROVEEDOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.Escenario;
import ar.edu.uade.skillswap.aplicacion.casouso.calificacion.CalificacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.calificacion.CalificarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.CalificacionNoPermitida;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

class CalificacionCasosDeUsoTest {

    private Escenario e;
    private Long publicacion;

    @BeforeEach
    void preparar() {
        e = new Escenario().conPerfiles();
        publicacion = e.publicacionPublicada();
    }

    @Test
    void completarCalificarUnaSolaVezYRecalcularLaReputacion() {
        ContratacionVista completada = e.completada(publicacion);

        CalificacionVista primera = e.calificar.ejecutar(new CalificarComando(CLIENTE, completada.id(), new Puntaje(5), "Excelente"));
        assertEquals(PROVEEDOR, primera.receptor());
        assertEquals(5.0, e.perfiles.obtenerPorUsuario(PROVEEDOR).getReputacion().promedio());

        assertThrows(CalificacionNoPermitida.class,
                () -> e.calificar.ejecutar(new CalificarComando(CLIENTE, completada.id(), new Puntaje(1), null)));
        assertEquals(1, e.perfiles.obtenerPorUsuario(PROVEEDOR).getReputacion().cantidad());
        assertEquals(1, e.calificaciones.cantidad());
    }

    @Test
    void cadaParteCalificaALaOtra() {
        ContratacionVista completada = e.completada(publicacion);

        e.calificar.ejecutar(new CalificarComando(CLIENTE, completada.id(), new Puntaje(5), null));
        e.calificar.ejecutar(new CalificarComando(PROVEEDOR, completada.id(), new Puntaje(3), null));

        assertEquals(5.0, e.perfiles.obtenerPorUsuario(PROVEEDOR).getReputacion().promedio());
        assertEquals(3.0, e.perfiles.obtenerPorUsuario(CLIENTE).getReputacion().promedio());
    }

    @Test
    void laReputacionEsElPromedioDeLasCalificacionesRecibidas() {
        ContratacionVista primera = e.completada(publicacion);
        e.calificar.ejecutar(new CalificarComando(CLIENTE, primera.id(), new Puntaje(5), null));
        ContratacionVista segunda = e.confirmada(publicacion, Escenario.F2);
        e.iniciar.ejecutar(new ContratacionComando(PROVEEDOR, segunda.id()));
        e.completar.ejecutar(new ContratacionComando(PROVEEDOR, segunda.id()));

        e.calificar.ejecutar(new CalificarComando(CLIENTE, segunda.id(), new Puntaje(4), null));

        assertEquals(4.5, e.perfiles.obtenerPorUsuario(PROVEEDOR).getReputacion().promedio());
        assertEquals(2, e.perfiles.obtenerPorUsuario(PROVEEDOR).getReputacion().cantidad());
    }

    @Test
    void soloSeCalificaUnaContratacionCompletada() {
        ContratacionVista confirmada = e.confirmada(publicacion, F1);

        assertThrows(CalificacionNoPermitida.class,
                () -> e.calificar.ejecutar(new CalificarComando(CLIENTE, confirmada.id(), new Puntaje(5), null)));
        assertEquals(0, e.calificaciones.cantidad());
    }

    @Test
    void soloLosParticipantesCalifican() {
        ContratacionVista completada = e.completada(publicacion);

        assertThrows(CalificacionNoPermitida.class, () -> e.calificar.ejecutar(
                new CalificarComando(new UsuarioId("ajeno"), completada.id(), new Puntaje(5), null)));
    }

    @Test
    void elPuntajeDebeEstarEntreUnoYCinco() {
        assertThrows(ValorInvalido.class, () -> new Puntaje(0));
        assertThrows(ValorInvalido.class, () -> new Puntaje(6));
    }
}
