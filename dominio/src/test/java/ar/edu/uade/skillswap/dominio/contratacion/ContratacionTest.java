package ar.edu.uade.skillswap.dominio.contratacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.Fixtures;
import ar.edu.uade.skillswap.dominio.cancelacion.PoliticaFlexible;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.error.FranjaNoDisponible;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;
import ar.edu.uade.skillswap.dominio.error.SolicitudNoPermitida;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

class ContratacionTest {

    private static final String ANTES_DE_LA_SESION_15H = "2026-11-10T00:00:00Z";
    private static final String ANTES_DE_LA_SESION_5H = "2026-11-10T10:00:00Z";

    @Test
    void solicitarCreaUnaContratacionPendienteConElPrecioVigente() {
        Contratacion contratacion = Fixtures.contratacionPendiente();

        assertEquals("PENDIENTE", contratacion.getNombreEstado());
        assertEquals(Fixtures.CLIENTE, contratacion.getClienteId());
        assertEquals(Fixtures.PROVEEDOR, contratacion.getProveedorId());
        assertEquals(SkillCoins.de(100), contratacion.getPrecioAcordado());
        assertEquals(SkillCoins.CERO, contratacion.getMontoRetenido());
        assertFalse(contratacion.estaComprometida());
    }

    @Test
    void elAutorNoPuedeSolicitarSuPropiaPublicacion() {
        Publicacion publicacion = Fixtures.publicacionPublicada();

        assertThrows(SolicitudNoPermitida.class, () -> Contratacion.solicitar(
                publicacion, Fixtures.perfilProveedor(), "m", "o", Fixtures.FRANJA, Fixtures.AHORA));
    }

    @Test
    void unaPublicacionNoPublicadaNoAdmiteSolicitudes() {
        Publicacion borrador = Fixtures.borrador();
        Publicacion pausada = Fixtures.publicacionPublicada();
        pausada.pausar(Fixtures.ctx(Fixtures.PROVEEDOR));

        assertThrows(SolicitudNoPermitida.class, () -> Contratacion.solicitar(
                borrador, Fixtures.perfilCliente(), "m", "o", Fixtures.FRANJA, Fixtures.AHORA));
        assertThrows(SolicitudNoPermitida.class, () -> Contratacion.solicitar(
                pausada, Fixtures.perfilCliente(), "m", "o", Fixtures.FRANJA, Fixtures.AHORA));
    }

    @Test
    void noSePuedeSolicitarUnaFranjaOcupadaOInexistente() {
        Publicacion publicacion = Fixtures.publicacionPublicada();
        publicacion.reservarCupo(Fixtures.FRANJA);

        assertThrows(FranjaNoDisponible.class, () -> Contratacion.solicitar(
                publicacion, Fixtures.perfilCliente(), "m", "o", Fixtures.FRANJA, Fixtures.AHORA));
        assertThrows(FranjaNoDisponible.class, () -> Contratacion.solicitar(publicacion, Fixtures.perfilCliente(), "m", "o",
                Fixtures.franja("2030-01-01T10:00:00Z", "2030-01-01T11:00:00Z"), Fixtures.AHORA));
    }

    @Test
    void unPerfilDadoDeBajaNoPuedeSolicitar() {
        var cliente = Fixtures.perfilCliente();
        cliente.darDeBaja(Fixtures.AHORA);

        assertThrows(SolicitudNoPermitida.class, () -> Contratacion.solicitar(
                Fixtures.publicacionPublicada(), cliente, "m", "o", Fixtures.FRANJA, Fixtures.AHORA));
    }

    @Test
    void recorreElFlujoCompletoDeContratacion() {
        Contratacion contratacion = Fixtures.contratacionPendiente();

        contratacion.aceptar(Fixtures.ctx(Fixtures.PROVEEDOR));
        assertEquals("ACEPTADA", contratacion.getNombreEstado());
        assertTrue(contratacion.estaComprometida());

        contratacion.confirmarPago(Fixtures.ctx(Fixtures.CLIENTE));
        assertEquals("CONFIRMADA", contratacion.getNombreEstado());
        assertEquals(SkillCoins.de(100), contratacion.getMontoRetenido());

        contratacion.iniciar(Fixtures.ctx(Fixtures.PROVEEDOR));
        assertEquals("EN_CURSO", contratacion.getNombreEstado());

        SkillCoins liberado = contratacion.completar(Fixtures.ctx(Fixtures.PROVEEDOR));
        assertEquals("COMPLETADA", contratacion.getNombreEstado());
        assertEquals(SkillCoins.de(100), liberado);
        assertEquals(SkillCoins.CERO, contratacion.getMontoRetenido());
        assertTrue(contratacion.estaCompletada());
        assertFalse(contratacion.estaComprometida());
    }

    @Test
    void cadaTransicionQuedaRegistradaConFechaActorYMotivo() {
        Contratacion contratacion = Fixtures.contratacionCompletada();

        var transiciones = contratacion.getHistorial().getTransiciones();

        assertEquals(5, transiciones.size());
        assertEquals("NINGUNO", transiciones.get(0).origen());
        assertEquals("PENDIENTE", transiciones.get(1).origen());
        assertEquals("ACEPTADA", transiciones.get(1).destino());
        assertEquals(Fixtures.PROVEEDOR, transiciones.get(1).actorId());
        assertNotNull(transiciones.get(1).fecha());
        assertEquals("Pago confirmado", transiciones.get(2).motivo());
    }

    @Test
    void lasAccionesFueraDeEstadoSeRechazan() {
        Contratacion pendiente = Fixtures.contratacionPendiente();
        Contratacion aceptada = Fixtures.contratacionAceptada();
        Contratacion confirmada = Fixtures.contratacionConfirmada();
        Contratacion enCurso = Fixtures.contratacionEnCurso();

        assertThrows(TransicionInvalida.class, () -> pendiente.confirmarPago(Fixtures.ctx(Fixtures.CLIENTE)));
        assertThrows(TransicionInvalida.class, () -> pendiente.iniciar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> pendiente.completar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> aceptada.aceptar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> aceptada.rechazar(Fixtures.ctx(Fixtures.PROVEEDOR), "motivo"));
        assertThrows(TransicionInvalida.class, () -> aceptada.iniciar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> confirmada.confirmarPago(Fixtures.ctx(Fixtures.CLIENTE)));
        assertThrows(TransicionInvalida.class, () -> confirmada.completar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> enCurso.cancelar(
                Fixtures.ctx(Fixtures.CLIENTE), PoliticaFlexible.porDefecto(), "motivo"));
        assertThrows(TransicionInvalida.class, () -> enCurso.proponerReprogramacion(
                Fixtures.ctx(Fixtures.CLIENTE), Fixtures.OTRA_FRANJA));
        assertThrows(TransicionInvalida.class, () -> enCurso.responderReprogramacion(Fixtures.ctx(Fixtures.CLIENTE), true));
    }

    @Test
    void unaContratacionCompletadaOTerminadaSoloSeConsulta() {
        Contratacion completada = Fixtures.contratacionCompletada();
        Contratacion rechazada = Fixtures.contratacionPendiente();
        rechazada.rechazar(Fixtures.ctx(Fixtures.PROVEEDOR), "No disponible");

        assertThrows(TransicionInvalida.class, () -> completada.cancelar(
                Fixtures.ctx(Fixtures.CLIENTE), PoliticaFlexible.porDefecto(), "motivo"));
        assertThrows(TransicionInvalida.class, () -> rechazada.aceptar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> rechazada.generarComprobante());
    }

    @Test
    void soloElProveedorAceptaRechazaIniciaYCompleta() {
        Contratacion pendiente = Fixtures.contratacionPendiente();
        Contratacion confirmada = Fixtures.contratacionConfirmada();
        Contratacion enCurso = Fixtures.contratacionEnCurso();

        assertThrows(PermisoDenegado.class, () -> pendiente.aceptar(Fixtures.ctx(Fixtures.CLIENTE)));
        assertThrows(PermisoDenegado.class, () -> pendiente.rechazar(Fixtures.ctx(Fixtures.CLIENTE), "motivo"));
        assertThrows(PermisoDenegado.class, () -> confirmada.iniciar(Fixtures.ctx(Fixtures.CLIENTE)));
        assertThrows(PermisoDenegado.class, () -> enCurso.completar(Fixtures.ctx(Fixtures.CLIENTE)));
    }

    @Test
    void soloElClientePaga() {
        Contratacion aceptada = Fixtures.contratacionAceptada();

        assertThrows(PermisoDenegado.class, () -> aceptada.confirmarPago(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertEquals("ACEPTADA", aceptada.getNombreEstado());
    }

    @Test
    void rechazarExigeMotivo() {
        Contratacion pendiente = Fixtures.contratacionPendiente();

        assertThrows(ValorInvalido.class, () -> pendiente.rechazar(Fixtures.ctx(Fixtures.PROVEEDOR), ""));
    }

    @Test
    void elSistemaPuedeRechazarSolicitudesPendientes() {
        Contratacion pendiente = Fixtures.contratacionPendiente();

        pendiente.rechazar(ar.edu.uade.skillswap.dominio.comun.ContextoAccion.delSistema(Fixtures.AHORA), "La publicación se cerró");

        assertEquals("RECHAZADA", pendiente.getNombreEstado());
    }

    @Test
    void cancelarDesdeAceptadaNoGeneraMovimientos() {
        Contratacion aceptada = Fixtures.contratacionAceptada();

        ResultadoCancelacion resultado =
                aceptada.cancelar(Fixtures.ctx(Fixtures.CLIENTE), PoliticaFlexible.porDefecto(), "Ya no puedo");

        assertEquals("CANCELADA", aceptada.getNombreEstado());
        assertEquals(SkillCoins.CERO, resultado.reintegroCliente());
        assertEquals(SkillCoins.CERO, resultado.pagoProveedor());
    }

    @Test
    void elClienteRecibeElReintegroSegunLaAnticipacion() {
        Contratacion conAnticipacion = Fixtures.contratacionConfirmada();
        Contratacion conPocaAnticipacion = Fixtures.contratacionConfirmada();
        Contratacion sinAnticipacion = Fixtures.contratacionConfirmada();

        ResultadoCancelacion total = conAnticipacion.cancelar(
                Fixtures.ctx(Fixtures.CLIENTE), PoliticaFlexible.porDefecto(), "motivo");
        ResultadoCancelacion parcial = conPocaAnticipacion.cancelar(
                Fixtures.ctx(Fixtures.CLIENTE, ANTES_DE_LA_SESION_15H), PoliticaFlexible.porDefecto(), "motivo");
        ResultadoCancelacion nulo = sinAnticipacion.cancelar(
                Fixtures.ctx(Fixtures.CLIENTE, ANTES_DE_LA_SESION_5H), PoliticaFlexible.porDefecto(), "motivo");

        assertEquals(SkillCoins.de(100), total.reintegroCliente());
        assertEquals(SkillCoins.CERO, total.pagoProveedor());
        assertEquals(SkillCoins.de(50), parcial.reintegroCliente());
        assertEquals(SkillCoins.de(50), parcial.pagoProveedor());
        assertEquals(SkillCoins.CERO, nulo.reintegroCliente());
        assertEquals(SkillCoins.de(100), nulo.pagoProveedor());
        assertEquals(SkillCoins.CERO, sinAnticipacion.getMontoRetenido());
    }

    @Test
    void siCancelaElProveedorElClienteRecuperaTodo() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();

        ResultadoCancelacion resultado = confirmada.cancelar(
                Fixtures.ctx(Fixtures.PROVEEDOR, ANTES_DE_LA_SESION_5H), PoliticaFlexible.porDefecto(), "Imprevisto");

        assertEquals(SkillCoins.de(100), resultado.reintegroCliente());
        assertEquals(SkillCoins.CERO, resultado.pagoProveedor());
    }

    @Test
    void unaCancelacionDespuesDelInicioDeLaFranjaNoReintegra() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();

        ResultadoCancelacion resultado = confirmada.cancelar(
                Fixtures.ctx(Fixtures.CLIENTE, "2026-11-10T16:30:00Z"), PoliticaFlexible.porDefecto(), "Tarde");

        assertEquals(SkillCoins.CERO, resultado.reintegroCliente());
    }

    @Test
    void soloLosParticipantesPuedenCancelar() {
        Contratacion aceptada = Fixtures.contratacionAceptada();
        Contratacion confirmada = Fixtures.contratacionConfirmada();
        var ajeno = Fixtures.ctx(new ar.edu.uade.skillswap.dominio.comun.UsuarioId("ajeno"));

        assertThrows(PermisoDenegado.class, () -> aceptada.cancelar(ajeno, PoliticaFlexible.porDefecto(), "m"));
        assertThrows(PermisoDenegado.class, () -> confirmada.cancelar(ajeno, PoliticaFlexible.porDefecto(), "m"));
    }

    @Test
    void cancelarExigeMotivo() {
        Contratacion aceptada = Fixtures.contratacionAceptada();

        assertThrows(ValorInvalido.class,
                () -> aceptada.cancelar(Fixtures.ctx(Fixtures.CLIENTE), PoliticaFlexible.porDefecto(), null));
    }

    @Test
    void unaAceptacionSinPagoVenceAlCumplirseElPlazo() {
        Contratacion aceptada = Fixtures.contratacionAceptada();
        Duration plazo = Duration.ofHours(48);

        assertFalse(aceptada.vencerSinPago(Fixtures.AHORA.plus(Duration.ofHours(47)), plazo));
        assertEquals("ACEPTADA", aceptada.getNombreEstado());

        assertTrue(aceptada.vencerSinPago(Fixtures.AHORA.plus(plazo), plazo));
        assertEquals("CANCELADA", aceptada.getNombreEstado());
        assertEquals(ar.edu.uade.skillswap.dominio.comun.UsuarioId.SISTEMA, aceptada.getHistorial().getTransiciones().get(2).actorId());
    }

    @Test
    void soloVenceUnaContratacionAceptada() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();
        Contratacion pendiente = Fixtures.contratacionPendiente();

        assertFalse(confirmada.vencerSinPago(Fixtures.AHORA.plusSeconds(999999), Duration.ofHours(1)));
        assertFalse(pendiente.vencerSinPago(Fixtures.AHORA.plusSeconds(999999), Duration.ofHours(1)));
    }

    @Test
    void laReprogramacionRequiereLaConformidadDeLaOtraParte() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();

        confirmada.proponerReprogramacion(Fixtures.ctx(Fixtures.CLIENTE), Fixtures.OTRA_FRANJA);
        assertEquals(Fixtures.FRANJA, confirmada.getFranja());
        assertTrue(confirmada.getPropuesta().estaPendiente());

        confirmada.responderReprogramacion(Fixtures.ctx(Fixtures.PROVEEDOR), true);

        assertEquals(Fixtures.OTRA_FRANJA, confirmada.getFranja());
        assertEquals(EstadoPropuesta.ACEPTADA, confirmada.getPropuesta().getEstado());
        assertEquals("CONFIRMADA", confirmada.getNombreEstado());
    }

    @Test
    void laOtraParteTambienPuedeRechazarLaReprogramacion() {
        Contratacion aceptada = Fixtures.contratacionAceptada();
        aceptada.proponerReprogramacion(Fixtures.ctx(Fixtures.PROVEEDOR), Fixtures.OTRA_FRANJA);

        aceptada.responderReprogramacion(Fixtures.ctx(Fixtures.CLIENTE), false);

        assertEquals(Fixtures.FRANJA, aceptada.getFranja());
        assertEquals(EstadoPropuesta.RECHAZADA, aceptada.getPropuesta().getEstado());
    }

    @Test
    void quienProponeNoPuedeResponderSuPropiaPropuesta() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();
        confirmada.proponerReprogramacion(Fixtures.ctx(Fixtures.CLIENTE), Fixtures.OTRA_FRANJA);

        assertThrows(PermisoDenegado.class, () -> confirmada.responderReprogramacion(Fixtures.ctx(Fixtures.CLIENTE), true));
    }

    @Test
    void soloPuedeHaberUnaPropuestaPendiente() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();
        confirmada.proponerReprogramacion(Fixtures.ctx(Fixtures.CLIENTE), Fixtures.OTRA_FRANJA);

        assertThrows(TransicionInvalida.class,
                () -> confirmada.proponerReprogramacion(Fixtures.ctx(Fixtures.PROVEEDOR), Fixtures.OTRA_FRANJA));
    }

    @Test
    void noSePuedeResponderSinPropuestaPendiente() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();

        assertThrows(TransicionInvalida.class, () -> confirmada.responderReprogramacion(Fixtures.ctx(Fixtures.PROVEEDOR), true));
    }

    @Test
    void soloLosParticipantesReprograman() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();

        assertThrows(PermisoDenegado.class, () -> confirmada.proponerReprogramacion(
                Fixtures.ctx(new ar.edu.uade.skillswap.dominio.comun.UsuarioId("ajeno")), Fixtures.OTRA_FRANJA));
    }

    @Test
    void elComprobanteSoloEstaDisponibleCuandoSeCompleta() {
        Contratacion enCurso = Fixtures.contratacionEnCurso();
        Contratacion completada = Fixtures.contratacionCompletada();

        assertThrows(TransicionInvalida.class, enCurso::generarComprobante);
        Comprobante comprobante = completada.generarComprobante();
        assertEquals(10L, comprobante.contratacionId());
        assertEquals(SkillCoins.de(100), comprobante.monto());
        assertEquals(Fixtures.CLIENTE, comprobante.clienteId());
    }

    @Test
    void identificaALasPartes() {
        Contratacion contratacion = Fixtures.contratacionPendiente();

        assertTrue(contratacion.esProveedor(Fixtures.PROVEEDOR));
        assertTrue(contratacion.esCliente(Fixtures.CLIENTE));
        assertTrue(contratacion.esParticipante(Fixtures.CLIENTE));
        assertFalse(contratacion.esParticipante(new ar.edu.uade.skillswap.dominio.comun.UsuarioId("ajeno")));
        assertEquals(Fixtures.CLIENTE, contratacion.contraparteDe(Fixtures.PROVEEDOR));
        assertEquals(Fixtures.PROVEEDOR, contratacion.contraparteDe(Fixtures.CLIENTE));
    }

    @Test
    void cadaAccionPublicaSuEventoParaLosDestinatariosCorrectos() {
        Contratacion contratacion = Fixtures.contratacionPendiente();
        List<EventoContratacion> solicitud = contratacion.extraerEventos();
        contratacion.aceptar(Fixtures.ctx(Fixtures.PROVEEDOR));
        List<EventoContratacion> aceptacion = contratacion.extraerEventos();
        contratacion.confirmarPago(Fixtures.ctx(Fixtures.CLIENTE));
        List<EventoContratacion> pago = contratacion.extraerEventos();
        contratacion.proponerReprogramacion(Fixtures.ctx(Fixtures.CLIENTE), Fixtures.OTRA_FRANJA);
        List<EventoContratacion> propuesta = contratacion.extraerEventos();
        contratacion.responderReprogramacion(Fixtures.ctx(Fixtures.PROVEEDOR), true);
        List<EventoContratacion> resolucion = contratacion.extraerEventos();
        contratacion.iniciar(Fixtures.ctx(Fixtures.PROVEEDOR));
        contratacion.completar(Fixtures.ctx(Fixtures.PROVEEDOR));
        List<EventoContratacion> completada = contratacion.extraerEventos();

        assertEquals(TipoEvento.SOLICITUD_RECIBIDA, solicitud.get(0).tipo());
        assertEquals(List.of(Fixtures.PROVEEDOR), solicitud.get(0).destinatarios());
        assertEquals(10L, solicitud.get(0).contratacionId());
        assertEquals(List.of(Fixtures.CLIENTE), aceptacion.get(0).destinatarios());
        assertEquals(TipoEvento.PAGO_CONFIRMADO, pago.get(0).tipo());
        assertEquals(List.of(Fixtures.CLIENTE, Fixtures.PROVEEDOR), pago.get(0).destinatarios());
        assertEquals(List.of(Fixtures.PROVEEDOR), propuesta.get(0).destinatarios());
        assertEquals(List.of(Fixtures.CLIENTE), resolucion.get(0).destinatarios());
        assertEquals(TipoEvento.SESION_COMPLETADA, completada.get(0).tipo());
    }

    @Test
    void extraerEventosLosVaciaYLaCancelacionAvisaALaOtraParte() {
        Contratacion confirmada = Fixtures.contratacionConfirmada();
        confirmada.extraerEventos();

        confirmada.cancelar(Fixtures.ctx(Fixtures.CLIENTE), PoliticaFlexible.porDefecto(), "motivo");
        List<EventoContratacion> eventos = confirmada.extraerEventos();

        assertEquals(TipoEvento.CONTRATACION_CANCELADA, eventos.get(0).tipo());
        assertEquals(List.of(Fixtures.PROVEEDOR), eventos.get(0).destinatarios());
        assertTrue(confirmada.extraerEventos().isEmpty());
    }

    @Test
    void elRechazoAvisaAlClienteYElVencimientoAAmbos() {
        Contratacion pendiente = Fixtures.contratacionPendiente();
        pendiente.extraerEventos();
        pendiente.rechazar(Fixtures.ctx(Fixtures.PROVEEDOR), "No puedo");
        Contratacion aceptada = Fixtures.contratacionAceptada();
        aceptada.extraerEventos();
        aceptada.vencerSinPago(Fixtures.AHORA.plus(Duration.ofHours(48)), Duration.ofHours(48));

        assertEquals(TipoEvento.SOLICITUD_RECHAZADA, pendiente.extraerEventos().get(0).tipo());
        assertEquals(List.of(Fixtures.CLIENTE, Fixtures.PROVEEDOR), aceptada.extraerEventos().get(0).destinatarios());
    }
}
