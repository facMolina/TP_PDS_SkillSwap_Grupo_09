package ar.edu.uade.skillswap.dominio;

import static ar.edu.uade.skillswap.dominio.Fixtures.AHORA;
import static ar.edu.uade.skillswap.dominio.Fixtures.CLIENTE;
import static ar.edu.uade.skillswap.dominio.Fixtures.FRANJA;
import static ar.edu.uade.skillswap.dominio.Fixtures.JAVA;
import static ar.edu.uade.skillswap.dominio.Fixtures.OTRA_FRANJA;
import static ar.edu.uade.skillswap.dominio.Fixtures.PROVEEDOR;
import static ar.edu.uade.skillswap.dominio.Fixtures.ctx;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.calificacion.Calificacion;
import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.comun.Reputacion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.contratacion.EstadoPropuesta;
import ar.edu.uade.skillswap.dominio.contratacion.InstantaneaContratacion;
import ar.edu.uade.skillswap.dominio.contratacion.PropuestaReprogramacion;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import ar.edu.uade.skillswap.dominio.perfil.CanalPreferido;
import ar.edu.uade.skillswap.dominio.perfil.EstadoPerfil;
import ar.edu.uade.skillswap.dominio.perfil.InstantaneaPerfil;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.dominio.publicacion.DatosPublicacion;
import ar.edu.uade.skillswap.dominio.publicacion.FranjaDisponible;
import ar.edu.uade.skillswap.dominio.publicacion.InstantaneaPublicacion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

class ReconstruccionTest {

    @Test
    void unaPublicacionReconstruidaConservaSuEstadoYSigueOperando() {
        Publicacion original = Fixtures.publicacionPublicada();
        original.reservarCupo(FRANJA);

        Publicacion reconstruida = Publicacion.reconstruir(instantanea(original, original.getNombreEstado()));

        assertEquals("PUBLICADA", reconstruida.getNombreEstado());
        assertEquals(1, reconstruida.getCuposTomados());
        assertEquals(2, reconstruida.getFranjas().size());
        assertTrue(reconstruida.getFranjas().get(0).estaOcupada());
        assertEquals(original.getHistorial().getTransiciones(), reconstruida.getHistorial().getTransiciones());
        assertEquals(7L, reconstruida.getVersion());
        reconstruida.pausar(ctx(PROVEEDOR));
        assertEquals("PAUSADA", reconstruida.getNombreEstado());
    }

    @Test
    void cadaEstadoDePublicacionPuedeReconstruirse() {
        Publicacion original = Fixtures.publicacionPublicada();

        for (String estado : List.of("BORRADOR", "PUBLICADA", "PAUSADA", "CERRADA", "CANCELADA")) {
            assertEquals(estado, Publicacion.reconstruir(instantanea(original, estado)).getNombreEstado());
        }
    }

    @Test
    void unEstadoDePublicacionDesconocidoSeRechaza() {
        Publicacion original = Fixtures.publicacionPublicada();

        assertThrows(ValorInvalido.class, () -> Publicacion.reconstruir(instantanea(original, "INEXISTENTE")));
    }

    @Test
    void cadaEstadoDeContratacionPuedeReconstruirseYSigueElFlujo() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();

        Contratacion reconstruida = Contratacion.reconstruir(instantanea(confirmada, "CONFIRMADA", null));

        assertEquals("CONFIRMADA", reconstruida.getNombreEstado());
        assertEquals(confirmada.getMontoRetenido(), reconstruida.getMontoRetenido());
        reconstruida.iniciar(ctx(PROVEEDOR));
        assertEquals("EN_CURSO", reconstruida.getNombreEstado());
        for (String estado : List.of("PENDIENTE", "ACEPTADA", "CONFIRMADA", "EN_CURSO", "COMPLETADA", "RECHAZADA",
                "CANCELADA")) {
            assertEquals(estado, Contratacion.reconstruir(instantanea(confirmada, estado, null)).getNombreEstado());
        }
    }

    @Test
    void unEstadoDeContratacionDesconocidoSeRechaza() {
        Contratacion original = Fixtures.contratacionPendiente();

        assertThrows(ValorInvalido.class, () -> Contratacion.reconstruir(instantanea(original, "INEXISTENTE", null)));
    }

    @Test
    void laContratacionReconstruidaConservaLaPropuestaPendiente() {
        Contratacion original = Fixtures.contratacionAceptada();
        PropuestaReprogramacion propuesta = new PropuestaReprogramacion(CLIENTE, OTRA_FRANJA, EstadoPropuesta.PENDIENTE);

        Contratacion reconstruida = Contratacion.reconstruir(instantanea(original, "ACEPTADA", propuesta));
        reconstruida.responderReprogramacion(ctx(PROVEEDOR), false);

        assertEquals(EstadoPropuesta.RECHAZADA, reconstruida.getPropuesta().getEstado());
        assertEquals(original.getFechaAceptacion(), reconstruida.getFechaAceptacion());
        assertNull(reconstruida.getFechaCompletada());
        assertThrows(TransicionInvalida.class, () -> reconstruida.aceptar(ctx(PROVEEDOR)));
    }

    @Test
    void unPerfilReconstruidoConservaTodosSusDatos() {
        InstantaneaPerfil instantanea = new InstantaneaPerfil(5L, CLIENTE, TipoPerfil.PERSONA, "Ana", "Bio",
                ZoneId.of("UTC"), "ana@mail.com", CanalPreferido.EMAIL, Set.of(JAVA), Set.of(), List.of(FRANJA),
                new Reputacion(9, 2), EstadoPerfil.BAJA, AHORA);

        Perfil perfil = Perfil.reconstruir(instantanea);

        assertEquals(5L, perfil.getId());
        assertEquals(4.5, perfil.getReputacion().promedio());
        assertEquals(CanalPreferido.EMAIL, perfil.getCanalPreferido());
        assertEquals(Set.of(JAVA), perfil.getHabilidades());
        assertEquals(List.of(FRANJA), perfil.getDisponibilidad());
        assertFalse(perfil.estaActivo());
        assertEquals(AHORA, perfil.getFechaBaja());
    }

    @Test
    void unaNotificacionYUnaCalificacionReconstruidasConservanSuIdentidad() {
        Notificacion notificacion = Notificacion.reconstruir(3L, CLIENTE, TipoEvento.SOLICITUD_ACEPTADA, "Mensaje",
                AHORA, true);
        Calificacion calificacion = Calificacion.reconstruir(4L, 10L, CLIENTE, PROVEEDOR, new Puntaje(5), "Bien", AHORA);

        assertEquals(3L, notificacion.getId());
        assertTrue(notificacion.estaLeida());
        assertEquals(4L, calificacion.getId());
        assertEquals(PROVEEDOR, calificacion.getReceptorId());
    }

    private InstantaneaPublicacion instantanea(Publicacion p, String estado) {
        DatosPublicacion datos = new DatosPublicacion(p.getTipo(), p.getTitulo(), p.getDescripcion(), p.getCategoria(),
                p.getModalidad(), p.getDuracionMinutos(), p.getCupo(), p.getPrecioBase(), p.getEtiquetas(),
                p.getTipoPoliticaCancelacion());
        List<FranjaDisponible> franjas = p.getFranjas().stream()
                .map(f -> new FranjaDisponible(f.getFranja(), f.estaOcupada()))
                .toList();
        return new InstantaneaPublicacion(1L, p.getAutorId(), datos, p.getCuposTomados(), franjas, p.getPromociones(),
                estado, p.getHistorial().getTransiciones(), p.getFechaCreacion(), 7L);
    }

    private InstantaneaContratacion instantanea(Contratacion c, String estado, PropuestaReprogramacion propuesta) {
        return new InstantaneaContratacion(c.getId(), c.getPublicacionId(), c.getClienteId(), c.getProveedorId(),
                c.getMensaje(), c.getObjetivo(), c.getFranja(), c.getPrecioAcordado(), c.getMontoRetenido(),
                c.getTipoPoliticaCancelacion(), c.getFechaSolicitud(), c.getFechaAceptacion(), c.getFechaCompletada(),
                propuesta, estado, c.getHistorial().getTransiciones());
    }
}
