package ar.edu.uade.skillswap.aplicacion.casouso;

import static ar.edu.uade.skillswap.aplicacion.Escenario.CLIENTE;
import static ar.edu.uade.skillswap.aplicacion.Escenario.F1;
import static ar.edu.uade.skillswap.aplicacion.Escenario.F2;
import static ar.edu.uade.skillswap.aplicacion.Escenario.F3;
import static ar.edu.uade.skillswap.aplicacion.Escenario.PROVEEDOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.Escenario;
import ar.edu.uade.skillswap.aplicacion.casouso.billetera.ConsultaBilletera;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.CancelarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.RechazarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ReprogramarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ResponderReprogramacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.SolicitarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AgregarPromocionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CambiarEstadoComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.TipoPromocion;
import ar.edu.uade.skillswap.aplicacion.comun.ConfiguracionAplicacion;
import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.dominio.billetera.TipoMovimiento;
import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.contratacion.Comprobante;
import ar.edu.uade.skillswap.dominio.error.CupoAgotado;
import ar.edu.uade.skillswap.dominio.error.FranjaNoDisponible;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;
import ar.edu.uade.skillswap.dominio.error.SaldoInsuficiente;
import ar.edu.uade.skillswap.dominio.error.SolicitudNoPermitida;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

class ContratacionCasosDeUsoTest {

    private Escenario e;

    @BeforeEach
    void preparar() {
        e = new Escenario().conPerfiles();
    }

    private ContratacionComando proveedor(Long id) {
        return new ContratacionComando(PROVEEDOR, id);
    }

    private ContratacionComando cliente(Long id) {
        return new ContratacionComando(CLIENTE, id);
    }

    @Test
    void solicitarAceptarYPagarConSaldoSuficiente() {
        Long publicacion = e.publicacionPublicada();

        ContratacionVista solicitada = e.solicitarFranja(publicacion, F1);
        assertEquals("PENDIENTE", solicitada.estado());
        assertEquals(SkillCoins.de(100), solicitada.precioAcordado());

        ContratacionVista aceptada = e.aceptar.ejecutar(proveedor(solicitada.id()));
        assertEquals("ACEPTADA", aceptada.estado());

        ContratacionVista pagada = e.pagar.ejecutar(cliente(solicitada.id()));
        assertEquals("CONFIRMADA", pagada.estado());
        assertEquals(SkillCoins.de(100), pagada.montoRetenido());
        assertEquals(SkillCoins.de(900), e.saldoDe(CLIENTE));
        assertEquals(SkillCoins.de(1000), e.saldoDe(PROVEEDOR));
    }

    @Test
    void alCompletarSeAcreditaAlProveedor() {
        Long publicacion = e.publicacionPublicada();

        ContratacionVista completada = e.completada(publicacion);

        assertEquals("COMPLETADA", completada.estado());
        assertEquals(SkillCoins.CERO, completada.montoRetenido());
        assertEquals(SkillCoins.de(900), e.saldoDe(CLIENTE));
        assertEquals(SkillCoins.de(1100), e.saldoDe(PROVEEDOR));
        assertEquals(TipoMovimiento.COBRO, e.billeteras.movimientosDe(PROVEEDOR).get(1).getTipo());
    }

    @Test
    void elAutorNoPuedeSolicitarSuPropiaPublicacion() {
        Long publicacion = e.publicacionPublicada();

        assertThrows(SolicitudNoPermitida.class, () -> e.solicitar.ejecutar(
                new SolicitarComando(PROVEEDOR, publicacion, "m", "o", F1)));
    }

    @Test
    void unaPublicacionNoPublicadaNoAdmiteSolicitudes() {
        Long publicacion = e.publicacionPublicada();
        e.cambiarEstado.ejecutar(new CambiarEstadoComando(
                PROVEEDOR, publicacion, AccionPublicacion.PAUSAR, null));

        assertThrows(SolicitudNoPermitida.class, () -> e.solicitarFranja(publicacion, F1));
    }

    @Test
    void dosAceptacionesNoPuedenConsumirElUltimoCupo() {
        UsuarioId otroCliente = new UsuarioId("cliente2");
        e.registrar(otroCliente, TipoPerfil.PERSONA);
        Long publicacion = e.publicacionPublicada(1, 100, TipoPoliticaCancelacion.FLEXIBLE);
        ContratacionVista primera = e.solicitarFranja(publicacion, F1);
        ContratacionVista segunda = e.solicitar.ejecutar(new SolicitarComando(otroCliente, publicacion, "m", "o", F2));

        e.aceptar.ejecutar(proveedor(primera.id()));

        assertThrows(CupoAgotado.class, () -> e.aceptar.ejecutar(proveedor(segunda.id())));
        Publicacion guardada = e.publicaciones.obtenerPorId(publicacion);
        assertEquals(1, guardada.getCuposTomados());
        assertFalse(guardada.tieneFranjaLibre(F1));
        assertTrue(guardada.tieneFranjaLibre(F2));
    }

    @Test
    void unConflictoDeConcurrenciaSeInformaComoCupoAgotado() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista solicitada = e.solicitarFranja(publicacion, F1);
        e.publicaciones.programarConflictoEnElProximoGuardado();

        assertThrows(CupoAgotado.class, () -> e.aceptar.ejecutar(proveedor(solicitada.id())));
    }

    @Test
    void noSePuedeSolicitarUnaFranjaYaReservada() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista primera = e.solicitarFranja(publicacion, F1);
        e.aceptar.ejecutar(proveedor(primera.id()));

        assertThrows(FranjaNoDisponible.class, () -> e.solicitarFranja(publicacion, F1));
    }

    @Test
    void soloElProveedorAceptaYSoloElClientePaga() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista solicitada = e.solicitarFranja(publicacion, F1);

        assertThrows(PermisoDenegado.class, () -> e.aceptar.ejecutar(cliente(solicitada.id())));
        e.aceptar.ejecutar(proveedor(solicitada.id()));
        assertThrows(PermisoDenegado.class, () -> e.pagar.ejecutar(proveedor(solicitada.id())));
        assertEquals(SkillCoins.de(1000), e.saldoDe(CLIENTE));
    }

    @Test
    void unPagoConSaldoInsuficienteNoModificaSaldosNiEstado() {
        Escenario pobre = new Escenario(new ConfiguracionAplicacion(SkillCoins.de(50), Duration.ofHours(48), 10))
                .conPerfiles();
        Long publicacion = pobre.publicacionPublicada();
        ContratacionVista solicitada = pobre.solicitarFranja(publicacion, F1);
        pobre.aceptar.ejecutar(proveedor(solicitada.id()));

        assertThrows(SaldoInsuficiente.class, () -> pobre.pagar.ejecutar(cliente(solicitada.id())));

        assertEquals(SkillCoins.de(50), pobre.saldoDe(CLIENTE));
        assertEquals("ACEPTADA", pobre.contrataciones.obtenerPorId(solicitada.id()).getNombreEstado());
        assertEquals(1, pobre.billeteras.movimientosDe(CLIENTE).size());
    }

    @Test
    void elDoblePagoSeRechazaSinDebitarDeNuevo() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista confirmada = e.confirmada(publicacion, F1);

        assertThrows(TransicionInvalida.class, () -> e.pagar.ejecutar(cliente(confirmada.id())));

        assertEquals(SkillCoins.de(900), e.saldoDe(CLIENTE));
    }

    @Test
    void elReintegroDependeDeLaPoliticaDeLaPublicacion() {
        Long flexible = e.publicacionPublicada(3, 100, TipoPoliticaCancelacion.FLEXIBLE);
        Long estricta = e.publicacionPublicada(3, 100, TipoPoliticaCancelacion.ESTRICTA);
        ContratacionVista conPoliticaFlexible = e.confirmada(flexible, F1);
        ContratacionVista conPoliticaEstricta = e.confirmada(estricta, F1);
        e.reloj.fijar(Instant.parse("2026-11-09T09:00:00Z"));

        e.cancelar.ejecutar(new CancelarComando(CLIENTE, conPoliticaFlexible.id(), "Cambio de planes"));
        assertEquals(SkillCoins.de(900), e.saldoDe(CLIENTE));
        assertEquals(SkillCoins.de(1000), e.saldoDe(PROVEEDOR));

        e.cancelar.ejecutar(new CancelarComando(CLIENTE, conPoliticaEstricta.id(), "Cambio de planes"));
        assertEquals(SkillCoins.de(900), e.saldoDe(CLIENTE));
        assertEquals(SkillCoins.de(1100), e.saldoDe(PROVEEDOR));
    }

    @Test
    void unReintegroParcialSeReparteEntreClienteYProveedor() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista confirmada = e.confirmada(publicacion, F1);
        e.reloj.fijar(Instant.parse("2026-11-10T00:00:00Z"));

        e.cancelar.ejecutar(new CancelarComando(CLIENTE, confirmada.id(), "Imprevisto"));

        assertEquals(SkillCoins.de(950), e.saldoDe(CLIENTE));
        assertEquals(SkillCoins.de(1050), e.saldoDe(PROVEEDOR));
        assertEquals(TipoMovimiento.REINTEGRO, e.billeteras.movimientosDe(CLIENTE).get(2).getTipo());
    }

    @Test
    void siCancelaElProveedorElClienteRecuperaTodo() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista confirmada = e.confirmada(publicacion, F1);
        e.reloj.fijar(Instant.parse("2026-11-10T14:00:00Z"));

        e.cancelar.ejecutar(new CancelarComando(PROVEEDOR, confirmada.id(), "No puedo asistir"));

        assertEquals(SkillCoins.de(1000), e.saldoDe(CLIENTE));
        assertEquals(SkillCoins.de(1000), e.saldoDe(PROVEEDOR));
    }

    @Test
    void cancelarLiberaElCupoYLaFranja() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista solicitada = e.solicitarFranja(publicacion, F1);
        e.aceptar.ejecutar(proveedor(solicitada.id()));
        assertEquals(1, e.publicaciones.obtenerPorId(publicacion).getCuposTomados());

        e.cancelar.ejecutar(new CancelarComando(CLIENTE, solicitada.id(), "Ya no lo necesito"));

        Publicacion guardada = e.publicaciones.obtenerPorId(publicacion);
        assertEquals(0, guardada.getCuposTomados());
        assertTrue(guardada.tieneFranjaLibre(F1));
        assertEquals(SkillCoins.de(1000), e.saldoDe(CLIENTE));
    }

    @Test
    void elProveedorPuedeRechazarUnaSolicitud() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista solicitada = e.solicitarFranja(publicacion, F1);

        ContratacionVista rechazada =
                e.rechazar.ejecutar(new RechazarComando(PROVEEDOR, solicitada.id(), "No tengo disponibilidad"));

        assertEquals("RECHAZADA", rechazada.estado());
        assertEquals(0, e.publicaciones.obtenerPorId(publicacion).getCuposTomados());
    }

    @Test
    void laReprogramacionMueveLaReservaCuandoLaOtraParteAcepta() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista confirmada = e.confirmada(publicacion, F1);

        ContratacionVista propuesta =
                e.proponerReprogramacion.ejecutar(new ReprogramarComando(CLIENTE, confirmada.id(), F2));
        assertTrue(propuesta.reprogramacionPendiente());
        assertEquals(F1, propuesta.franja());

        ContratacionVista resuelta = e.responderReprogramacion.ejecutar(
                new ResponderReprogramacionComando(PROVEEDOR, confirmada.id(), true));

        Publicacion guardada = e.publicaciones.obtenerPorId(publicacion);
        assertEquals(F2, resuelta.franja());
        assertFalse(resuelta.reprogramacionPendiente());
        assertTrue(guardada.tieneFranjaLibre(F1));
        assertFalse(guardada.tieneFranjaLibre(F2));
        assertEquals(1, guardada.getCuposTomados());
    }

    @Test
    void unaReprogramacionRechazadaDejaLaFranjaOriginal() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista confirmada = e.confirmada(publicacion, F1);
        e.proponerReprogramacion.ejecutar(new ReprogramarComando(PROVEEDOR, confirmada.id(), F2));

        ContratacionVista resuelta = e.responderReprogramacion.ejecutar(
                new ResponderReprogramacionComando(CLIENTE, confirmada.id(), false));

        assertEquals(F1, resuelta.franja());
        assertFalse(e.publicaciones.obtenerPorId(publicacion).tieneFranjaLibre(F1));
    }

    @Test
    void noSePuedeReprogramarAUnaFranjaOcupada() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista primera = e.confirmada(publicacion, F1);
        e.confirmada(publicacion, F2);

        assertThrows(FranjaNoDisponible.class,
                () -> e.proponerReprogramacion.ejecutar(new ReprogramarComando(CLIENTE, primera.id(), F2)));
    }

    @Test
    void siLaFranjaPropuestaSeOcupoAntesDeResponderNoSeMueveLaReserva() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista primera = e.confirmada(publicacion, F1);
        e.proponerReprogramacion.ejecutar(new ReprogramarComando(CLIENTE, primera.id(), F2));
        e.confirmada(publicacion, F2);

        assertThrows(FranjaNoDisponible.class, () -> e.responderReprogramacion.ejecutar(
                new ResponderReprogramacionComando(PROVEEDOR, primera.id(), true)));
        assertEquals(F1, e.contrataciones.obtenerPorId(primera.id()).getFranja());
    }

    @Test
    void unaAceptacionSinPagoVenceYLiberaElCupo() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista solicitada = e.solicitarFranja(publicacion, F1);
        e.aceptar.ejecutar(proveedor(solicitada.id()));

        e.reloj.avanzar(Duration.ofHours(47));
        assertEquals(0, e.vencer.ejecutar());

        e.reloj.avanzar(Duration.ofHours(1));
        assertEquals(1, e.vencer.ejecutar());

        Publicacion guardada = e.publicaciones.obtenerPorId(publicacion);
        assertEquals("CANCELADA", e.contrataciones.obtenerPorId(solicitada.id()).getNombreEstado());
        assertEquals(0, guardada.getCuposTomados());
        assertTrue(guardada.tieneFranjaLibre(F1));
        assertEquals(0, e.vencer.ejecutar());
    }

    @Test
    void lasPromocionesAcumulablesNuncaDejanElPrecioNegativo() {
        Long publicacion = e.publicacionPublicada();
        Instant desde = Escenario.INICIO.minus(Duration.ofDays(1));
        Instant hasta = Escenario.INICIO.plus(Duration.ofDays(30));
        e.agregarPromocion.ejecutar(new AgregarPromocionComando(
                PROVEEDOR, publicacion, TipoPromocion.PORCENTAJE, 60, desde, hasta));
        e.agregarPromocion.ejecutar(new AgregarPromocionComando(
                PROVEEDOR, publicacion, TipoPromocion.MONTO_FIJO, 70, desde, hasta));

        ContratacionVista solicitada = e.solicitarFranja(publicacion, F1);

        assertEquals(SkillCoins.CERO, solicitada.precioAcordado());
    }

    @Test
    void lasPromocionesSeAplicanEnOrdenYSeFijanAlSolicitar() {
        Long publicacion = e.publicacionPublicada();
        Instant desde = Escenario.INICIO.minus(Duration.ofDays(1));
        Instant hasta = Escenario.INICIO.plus(Duration.ofDays(30));
        e.agregarPromocion.ejecutar(new AgregarPromocionComando(
                PROVEEDOR, publicacion, TipoPromocion.PORCENTAJE, 20, desde, hasta));
        e.agregarPromocion.ejecutar(new AgregarPromocionComando(
                PROVEEDOR, publicacion, TipoPromocion.MONTO_FIJO, 30, desde, hasta));

        ContratacionVista solicitada = e.solicitarFranja(publicacion, F1);

        assertEquals(SkillCoins.de(50), solicitada.precioAcordado());
    }

    @Test
    void unaPromocionVencidaNoSeAplica() {
        Long publicacion = e.publicacionPublicada();
        e.agregarPromocion.ejecutar(new AgregarPromocionComando(
                PROVEEDOR, publicacion, TipoPromocion.PORCENTAJE, 50,
                Escenario.INICIO.minus(Duration.ofDays(30)), Escenario.INICIO.minus(Duration.ofDays(1))));

        assertEquals(SkillCoins.de(100), e.solicitarFranja(publicacion, F1).precioAcordado());
    }

    @Test
    void elComprobanteEstaDisponibleSoloParaLosParticipantesDeUnaSesionCompletada() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista confirmada = e.confirmada(publicacion, F1);

        assertThrows(TransicionInvalida.class, () -> e.comprobante.ejecutar(cliente(confirmada.id())));
        e.iniciar.ejecutar(proveedor(confirmada.id()));
        e.completar.ejecutar(proveedor(confirmada.id()));

        Comprobante comprobante = e.comprobante.ejecutar(cliente(confirmada.id()));
        assertEquals(SkillCoins.de(100), comprobante.monto());
        assertThrows(PermisoDenegado.class, () -> e.comprobante.ejecutar(
                new ContratacionComando(new UsuarioId("ajeno"), confirmada.id())));
    }

    @Test
    void laBilleteraInformaSaldoYMovimientos() {
        Long publicacion = e.publicacionPublicada();
        e.confirmada(publicacion, F1);

        var vista = e.consultarBilletera.ejecutar(new ConsultaBilletera(CLIENTE, null));

        assertEquals(SkillCoins.de(900), vista.saldo());
        assertEquals(2, vista.movimientos().total());
        assertEquals(TipoMovimiento.SALDO_INICIAL, vista.movimientos().elementos().get(0).tipo());
        assertEquals(TipoMovimiento.PAGO, vista.movimientos().elementos().get(1).tipo());
        assertEquals(SkillCoins.de(900), vista.movimientos().elementos().get(1).saldoResultante());
    }

    @Test
    void unaContratacionInexistenteSeInformaComoRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontrado.class, () -> e.aceptar.ejecutar(proveedor(999L)));
        assertThrows(RecursoNoEncontrado.class, () -> e.solicitarFranja(999L, F1));
    }

    @Test
    void iniciarYCompletarRespetanElOrdenDelFlujo() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista confirmada = e.confirmada(publicacion, F1);

        assertThrows(TransicionInvalida.class, () -> e.completar.ejecutar(proveedor(confirmada.id())));
        ContratacionVista enCurso = e.iniciar.ejecutar(proveedor(confirmada.id()));
        assertEquals("EN_CURSO", enCurso.estado());
        assertThrows(TransicionInvalida.class, () -> e.iniciar.ejecutar(proveedor(confirmada.id())));
    }

    @Test
    void elConflictoDeConcurrenciaAlAceptarSeInformaComoCupoAgotado() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista solicitada = e.solicitarFranja(publicacion, F3);
        e.publicaciones.programarConflictoEnElProximoGuardado();

        assertThrows(CupoAgotado.class, () -> e.aceptar.ejecutar(proveedor(solicitada.id())));
    }
}
