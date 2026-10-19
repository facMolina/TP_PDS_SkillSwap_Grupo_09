package ar.edu.uade.skillswap.dominio.calificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.Fixtures;
import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.error.CalificacionNoPermitida;

class CalificacionTest {

    @Test
    void elClienteCalificaAlProveedor() {
        Contratacion completada = Fixtures.contratacionCompletada();

        Calificacion calificacion =
                Calificacion.registrar(completada, Fixtures.CLIENTE, new Puntaje(5), "Excelente", Fixtures.AHORA);

        assertEquals(Fixtures.CLIENTE, calificacion.getAutorId());
        assertEquals(Fixtures.PROVEEDOR, calificacion.getReceptorId());
        assertEquals(5, calificacion.getPuntaje().valor());
        assertEquals(10L, calificacion.getContratacionId());
        assertEquals("Excelente", calificacion.getComentario());
    }

    @Test
    void elProveedorCalificaAlCliente() {
        Contratacion completada = Fixtures.contratacionCompletada();

        Calificacion calificacion =
                Calificacion.registrar(completada, Fixtures.PROVEEDOR, new Puntaje(4), null, Fixtures.AHORA);

        assertEquals(Fixtures.CLIENTE, calificacion.getReceptorId());
    }

    @Test
    void soloSePuedeCalificarUnaContratacionCompletada() {
        Contratacion enCurso = Fixtures.contratacionEnCurso();

        assertThrows(CalificacionNoPermitida.class,
                () -> Calificacion.registrar(enCurso, Fixtures.CLIENTE, new Puntaje(5), null, Fixtures.AHORA));
    }

    @Test
    void soloLosParticipantesPuedenCalificar() {
        Contratacion completada = Fixtures.contratacionCompletada();

        assertThrows(CalificacionNoPermitida.class, () -> Calificacion.registrar(
                completada, new UsuarioId("ajeno"), new Puntaje(1), null, Fixtures.AHORA));
    }
}
