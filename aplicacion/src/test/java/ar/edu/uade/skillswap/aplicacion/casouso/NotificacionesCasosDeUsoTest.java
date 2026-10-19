package ar.edu.uade.skillswap.aplicacion.casouso;

import static ar.edu.uade.skillswap.aplicacion.Escenario.CLIENTE;
import static ar.edu.uade.skillswap.aplicacion.Escenario.F1;
import static ar.edu.uade.skillswap.aplicacion.Escenario.PROVEEDOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.Escenario;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.notificacion.ListadoNotificaciones;
import ar.edu.uade.skillswap.aplicacion.casouso.notificacion.MarcarLeidaComando;
import ar.edu.uade.skillswap.aplicacion.casouso.notificacion.NotificacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.ActualizarPerfilComando;
import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.aplicacion.evento.GeneradorMensajes;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.perfil.CanalPreferido;

class NotificacionesCasosDeUsoTest {

    private Escenario e;
    private Long publicacion;

    @BeforeEach
    void preparar() {
        e = new Escenario().conPerfiles();
        publicacion = e.publicacionPublicada();
    }

    private void elegirCanal(UsuarioId usuario, CanalPreferido canal) {
        e.actualizarPerfil.ejecutar(new ActualizarPerfilComando(usuario, "Nombre", null, ZoneId.of("UTC"), null, null,
                null, canal));
    }

    @Test
    void unMismoEventoSeNotificaPorLosCanalesElegidosPorCadaParte() {
        elegirCanal(CLIENTE, CanalPreferido.EMAIL);
        elegirCanal(PROVEEDOR, CanalPreferido.PUSH);

        e.confirmada(publicacion, F1);

        verify(e.email).enviarCorreo(eq("cliente@mail.com"), anyString(), contains("pago"));
        verify(e.push).enviarPush(eq(PROVEEDOR), anyString(), contains("pago"));
    }

    @Test
    void cadaParteRecibeAvisosSoloPorSuCanal() {
        elegirCanal(CLIENTE, CanalPreferido.EMAIL);

        e.solicitarFranja(publicacion, F1);

        verifyNoInteractions(e.email);
        verifyNoInteractions(e.push);
        assertEquals(1, e.notificaciones.listarPorDestinatario(PROVEEDOR, SolicitudPagina.primera()).total());
        assertEquals(0, e.notificaciones.listarPorDestinatario(CLIENTE, SolicitudPagina.primera()).total());
    }

    @Test
    void elCanalInternoGuardaLaNotificacionEnLaBandeja() {
        e.solicitarFranja(publicacion, F1);

        Pagina<NotificacionVista> bandeja =
                e.listarNotificaciones.ejecutar(new ListadoNotificaciones(PROVEEDOR, null));

        assertEquals(1, bandeja.total());
        assertEquals(TipoEvento.SOLICITUD_RECIBIDA, bandeja.elementos().get(0).tipo());
        assertFalse(bandeja.elementos().get(0).leida());
        assertTrue(bandeja.elementos().get(0).mensaje().contains("solicitud"));
    }

    @Test
    void cadaEventoDelFlujoNotificaAlDestinatarioCorrecto() {
        ContratacionVista confirmada = e.confirmada(publicacion, F1);
        e.iniciar.ejecutar(new ContratacionComando(PROVEEDOR, confirmada.id()));
        e.completar.ejecutar(new ContratacionComando(PROVEEDOR, confirmada.id()));

        List<TipoEvento> paraElProveedor = tiposRecibidos(PROVEEDOR);
        List<TipoEvento> paraElCliente = tiposRecibidos(CLIENTE);

        assertEquals(List.of(TipoEvento.SOLICITUD_RECIBIDA, TipoEvento.PAGO_CONFIRMADO, TipoEvento.SESION_COMPLETADA),
                paraElProveedor);
        assertEquals(List.of(TipoEvento.SOLICITUD_ACEPTADA, TipoEvento.PAGO_CONFIRMADO, TipoEvento.SESION_COMPLETADA),
                paraElCliente);
    }

    private List<TipoEvento> tiposRecibidos(UsuarioId usuario) {
        return e.listarNotificaciones.ejecutar(new ListadoNotificaciones(usuario, null)).elementos().stream()
                .map(NotificacionVista::tipo)
                .toList();
    }

    @Test
    void marcarComoLeidaSoloLoPuedeHacerElDestinatario() {
        e.solicitarFranja(publicacion, F1);
        Long id = e.listarNotificaciones.ejecutar(new ListadoNotificaciones(PROVEEDOR, null)).elementos().get(0).id();

        assertThrows(PermisoDenegado.class, () -> e.marcarLeida.ejecutar(new MarcarLeidaComando(CLIENTE, id)));
        e.marcarLeida.ejecutar(new MarcarLeidaComando(PROVEEDOR, id));

        assertTrue(e.listarNotificaciones.ejecutar(new ListadoNotificaciones(PROVEEDOR, null))
                .elementos().get(0).leida());
    }

    @Test
    void marcarUnaNotificacionInexistenteFalla() {
        assertThrows(RecursoNoEncontrado.class, () -> e.marcarLeida.ejecutar(new MarcarLeidaComando(CLIENTE, 99L)));
    }

    @Test
    void laBandejaSePagina() {
        e.confirmada(publicacion, F1);

        Pagina<NotificacionVista> primera =
                e.listarNotificaciones.ejecutar(new ListadoNotificaciones(PROVEEDOR, new SolicitudPagina(0, 1)));
        Pagina<NotificacionVista> segunda =
                e.listarNotificaciones.ejecutar(new ListadoNotificaciones(PROVEEDOR, new SolicitudPagina(1, 1)));

        assertEquals(2, primera.total());
        assertEquals(1, primera.elementos().size());
        assertEquals(1, segunda.elementos().size());
    }

    @Test
    void notificarAUnPerfilInexistenteFalla() {
        EventoContratacion evento = new EventoContratacion(TipoEvento.PAGO_CONFIRMADO, 1L, UsuarioId.SISTEMA,
                List.of(new UsuarioId("fantasma")), Escenario.INICIO);

        assertThrows(RecursoNoEncontrado.class, () -> e.bus.publicar(evento));
        verify(e.push, never()).enviarPush(eq(new UsuarioId("fantasma")), anyString(), anyString());
    }

    @Test
    void elGeneradorTieneUnMensajeParaCadaTipoDeEvento() {
        GeneradorMensajes generador = new GeneradorMensajes();

        for (TipoEvento tipo : TipoEvento.values()) {
            String mensaje = generador.mensajePara(new EventoContratacion(tipo, 7L, UsuarioId.SISTEMA, List.of(),
                    Escenario.INICIO));
            assertTrue(mensaje.contains("7"), tipo + " debería incluir la contratación");
        }
    }
}
