package ar.edu.uade.skillswap.aplicacion.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.dobles.UnidadDeTrabajoDirecta;
import ar.edu.uade.skillswap.aplicacion.puerto.UnidadDeTrabajo;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;

class UtilidadesTest {

    private static final List<Integer> NUMEROS = List.of(1, 2, 3, 4, 5, 6, 7);

    @Test
    void laSolicitudDePaginaRechazaValoresFueraDeRango() {
        assertThrows(ValorInvalido.class, () -> new SolicitudPagina(-1, 10));
        assertThrows(ValorInvalido.class, () -> new SolicitudPagina(0, 0));
        assertThrows(ValorInvalido.class, () -> new SolicitudPagina(0, SolicitudPagina.TAMANIO_MAXIMO + 1));
    }

    @Test
    void laPrimeraPaginaUsaElTamanioPorDefecto() {
        SolicitudPagina primera = SolicitudPagina.primera();

        assertEquals(0, primera.numero());
        assertEquals(SolicitudPagina.TAMANIO_POR_DEFECTO, primera.tamanio());
        assertEquals(0, primera.desplazamiento());
        assertEquals(20, new SolicitudPagina(2, 10).desplazamiento());
    }

    @Test
    void laPaginacionDevuelveElTramoSolicitado() {
        Pagina<Integer> pagina = Pagina.de(NUMEROS, new SolicitudPagina(1, 3));

        assertEquals(List.of(4, 5, 6), pagina.elementos());
        assertEquals(7, pagina.total());
        assertEquals(3, pagina.totalPaginas());
    }

    @Test
    void unaPaginaFueraDeRangoQuedaVacia() {
        Pagina<Integer> pagina = Pagina.de(NUMEROS, new SolicitudPagina(5, 10));

        assertTrue(pagina.elementos().isEmpty());
        assertEquals(7, pagina.total());
    }

    @Test
    void laUltimaPaginaPuedeTenerMenosElementos() {
        Pagina<Integer> pagina = Pagina.de(NUMEROS, new SolicitudPagina(2, 3));

        assertEquals(List.of(7), pagina.elementos());
    }

    @Test
    void transformarConservaLaInformacionDePaginado() {
        Pagina<String> pagina = Pagina.de(NUMEROS, new SolicitudPagina(0, 2)).transformar(n -> "n" + n);

        assertEquals(List.of("n1", "n2"), pagina.elementos());
        assertEquals(7, pagina.total());
        assertEquals(2, pagina.tamanio());
    }

    @Test
    void elEjecutorPublicaLosEventosDespuesDeConfirmarLaUnidadDeTrabajo() {
        List<String> orden = new ArrayList<>();
        UnidadDeTrabajo unidad = new UnidadDeTrabajo() {
            @Override
            public <T> T ejecutar(Supplier<T> accion) {
                T resultado = accion.get();
                orden.add("commit");
                return resultado;
            }
        };
        EjecutorTransaccional ejecutor = new EjecutorTransaccional(unidad, evento -> orden.add("evento"));

        String valor = ejecutor.ejecutar(() -> ResultadoTransaccion.de("ok", List.of(evento(), evento())));

        assertEquals("ok", valor);
        assertEquals(List.of("commit", "evento", "evento"), orden);
    }

    @Test
    void elEjecutorNoPublicaEventosSiLaUnidadDeTrabajoFalla() {
        List<EventoContratacion> publicados = new ArrayList<>();
        EjecutorTransaccional ejecutor = new EjecutorTransaccional(new UnidadDeTrabajoDirecta(), publicados::add);

        assertThrows(IllegalStateException.class, () -> ejecutor.ejecutar(() -> {
            throw new IllegalStateException("falla");
        }));

        assertTrue(publicados.isEmpty());
    }

    @Test
    void lasConsultasNoPublicanEventos() {
        List<EventoContratacion> publicados = new ArrayList<>();
        EjecutorTransaccional ejecutor = new EjecutorTransaccional(new UnidadDeTrabajoDirecta(), publicados::add);

        assertEquals(42, ejecutor.consultar(() -> 42));
        assertTrue(publicados.isEmpty());
    }

    @Test
    void elResultadoSinEventosNoTraeEventos() {
        assertTrue(ResultadoTransaccion.sinEventos("x").eventos().isEmpty());
    }

    private static EventoContratacion evento() {
        return new EventoContratacion(TipoEvento.values()[0], 1L, UsuarioId.SISTEMA, List.of(UsuarioId.SISTEMA), Instant.EPOCH);
    }
}
