package ar.edu.uade.skillswap.aplicacion.casouso;

import static ar.edu.uade.skillswap.aplicacion.Escenario.CLIENTE;
import static ar.edu.uade.skillswap.aplicacion.Escenario.F1;
import static ar.edu.uade.skillswap.aplicacion.Escenario.F2;
import static ar.edu.uade.skillswap.aplicacion.Escenario.ORGANIZACION;
import static ar.edu.uade.skillswap.aplicacion.Escenario.PROVEEDOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.Escenario;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AgregarPromocionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CambiarEstadoComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CrearBorradorComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.DatosPublicacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.EditarBorradorComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.PublicacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.TipoPromocion;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.error.OperacionNoPermitida;
import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;
import ar.edu.uade.skillswap.dominio.error.PublicacionIncompleta;
import ar.edu.uade.skillswap.dominio.error.SolicitudNoPermitida;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;

class PublicacionCasosDeUsoTest {

    private Escenario e;

    @BeforeEach
    void preparar() {
        e = new Escenario().conPerfiles();
    }

    private CambiarEstadoComando accion(AccionPublicacion accion, Long publicacion, String motivo) {
        return new CambiarEstadoComando(PROVEEDOR, publicacion, accion, motivo);
    }

    @Test
    void crearBorradorResuelveLaCategoriaYLasEtiquetasDelCatalogo() {
        PublicacionVista vista = e.crearBorrador.ejecutar(new CrearBorradorComando(
                PROVEEDOR, e.datos(2, 100, TipoPoliticaCancelacion.ESTRICTA), List.of(F1, F2)));

        assertEquals("BORRADOR", vista.estado());
        assertEquals("Programación", vista.categoria());
        assertEquals(2, vista.franjas());
        assertEquals(TipoPoliticaCancelacion.ESTRICTA, vista.politicaCancelacion());
    }

    @Test
    void unaCategoriaOUnaEtiquetaInexistenteSeRechazan() {
        DatosPublicacionComando sinCategoria = new DatosPublicacionComando(TipoPublicacion.MENTORIA, "t", "d", 99L,
                Modalidad.REMOTA, 60, 1, SkillCoins.de(1), Set.of(), null);
        DatosPublicacionComando sinEtiqueta = new DatosPublicacionComando(TipoPublicacion.MENTORIA, "t", "d", 1L,
                Modalidad.REMOTA, 60, 1, SkillCoins.de(1), Set.of(10L, 777L), null);

        assertThrows(RecursoNoEncontrado.class,
                () -> e.crearBorrador.ejecutar(new CrearBorradorComando(PROVEEDOR, sinCategoria, null)));
        assertThrows(RecursoNoEncontrado.class,
                () -> e.crearBorrador.ejecutar(new CrearBorradorComando(PROVEEDOR, sinEtiqueta, null)));
    }

    @Test
    void laPersonaNoPublicaDesafiosYLaOrganizacionSi() {
        e.registrar(ORGANIZACION, TipoPerfil.ORGANIZACION);
        DatosPublicacionComando desafio = new DatosPublicacionComando(TipoPublicacion.DESAFIO, "Desafío", "d", 1L,
                Modalidad.REMOTA, 60, 3, SkillCoins.de(10), Set.of(), null);

        assertThrows(PermisoDenegado.class,
                () -> e.crearBorrador.ejecutar(new CrearBorradorComando(PROVEEDOR, desafio, null)));
        assertEquals(TipoPublicacion.DESAFIO,
                e.crearBorrador.ejecutar(new CrearBorradorComando(ORGANIZACION, desafio, null)).tipo());
    }

    @Test
    void unPerfilInexistenteNoPuedeCrearBorradores() {
        assertThrows(RecursoNoEncontrado.class, () -> e.crearBorrador.ejecutar(new CrearBorradorComando(
                new UsuarioId("nadie"), e.datos(1, 1, null), null)));
    }

    @Test
    void unaOfertaIncompletaNoSePuedePublicar() {
        DatosPublicacionComando incompleta = new DatosPublicacionComando(TipoPublicacion.MENTORIA, "", null, null,
                Modalidad.REMOTA, 60, 1, null, null, null);
        PublicacionVista borrador =
                e.crearBorrador.ejecutar(new CrearBorradorComando(PROVEEDOR, incompleta, null));

        assertThrows(PublicacionIncompleta.class,
                () -> e.cambiarEstado.ejecutar(accion(AccionPublicacion.PUBLICAR, borrador.id(), null)));
        assertEquals("BORRADOR", e.publicaciones.obtenerPorId(borrador.id()).getNombreEstado());
    }

    @Test
    void completarElBorradorPermitePublicarlo() {
        DatosPublicacionComando incompleta = new DatosPublicacionComando(TipoPublicacion.MENTORIA, "Título", null, null,
                Modalidad.REMOTA, 60, 1, null, null, null);
        PublicacionVista borrador =
                e.crearBorrador.ejecutar(new CrearBorradorComando(PROVEEDOR, incompleta, List.of(F1)));

        e.editarBorrador.ejecutar(
                new EditarBorradorComando(PROVEEDOR, borrador.id(), e.datos(1, 50, TipoPoliticaCancelacion.FLEXIBLE)));
        PublicacionVista publicada =
                e.cambiarEstado.ejecutar(accion(AccionPublicacion.PUBLICAR, borrador.id(), null));

        assertEquals("PUBLICADA", publicada.estado());
        assertEquals(SkillCoins.de(50), publicada.precioBase());
    }

    @Test
    void soloElAutorEditaPublicaYCambiaElEstado() {
        PublicacionVista borrador = e.crearBorrador.ejecutar(
                new CrearBorradorComando(PROVEEDOR, e.datos(1, 10, null), List.of(F1)));

        assertThrows(PermisoDenegado.class, () -> e.editarBorrador.ejecutar(
                new EditarBorradorComando(CLIENTE, borrador.id(), e.datos(1, 10, null))));
        assertThrows(PermisoDenegado.class, () -> e.cambiarEstado.ejecutar(
                new CambiarEstadoComando(CLIENTE, borrador.id(), AccionPublicacion.PUBLICAR, null)));
    }

    @Test
    void unaPublicacionPublicadaYaNoSeEdita() {
        Long publicacion = e.publicacionPublicada();

        assertThrows(TransicionInvalida.class, () -> e.editarBorrador.ejecutar(
                new EditarBorradorComando(PROVEEDOR, publicacion, e.datos(1, 10, null))));
    }

    @Test
    void unaPublicacionPausadaNoAdmiteNuevasSolicitudes() {
        Long publicacion = e.publicacionPublicada();

        PublicacionVista pausada = e.cambiarEstado.ejecutar(accion(AccionPublicacion.PAUSAR, publicacion, null));

        assertEquals("PAUSADA", pausada.estado());
        assertThrows(SolicitudNoPermitida.class, () -> e.solicitarFranja(publicacion, F1));
    }

    @Test
    void reanudarYCerrarRespetanElCiclo() {
        Long publicacion = e.publicacionPublicada();
        e.cambiarEstado.ejecutar(accion(AccionPublicacion.PAUSAR, publicacion, null));

        assertEquals("PUBLICADA", e.cambiarEstado.ejecutar(accion(AccionPublicacion.REANUDAR, publicacion, null)).estado());
        assertEquals("CERRADA", e.cambiarEstado.ejecutar(accion(AccionPublicacion.CERRAR, publicacion, null)).estado());
        assertThrows(TransicionInvalida.class,
                () -> e.cambiarEstado.ejecutar(accion(AccionPublicacion.PUBLICAR, publicacion, null)));
    }

    @Test
    void cancelarExigeUnMotivo() {
        Long publicacion = e.publicacionPublicada();

        assertThrows(ValorInvalido.class,
                () -> e.cambiarEstado.ejecutar(accion(AccionPublicacion.CANCELAR, publicacion, " ")));
        assertEquals("CANCELADA",
                e.cambiarEstado.ejecutar(accion(AccionPublicacion.CANCELAR, publicacion, "Ya no la ofrezco")).estado());
    }

    @Test
    void noSePuedeCerrarNiCancelarConContratacionesComprometidas() {
        Long publicacion = e.publicacionPublicada();
        e.confirmada(publicacion, F1);

        assertThrows(OperacionNoPermitida.class,
                () -> e.cambiarEstado.ejecutar(accion(AccionPublicacion.CERRAR, publicacion, null)));
        assertThrows(OperacionNoPermitida.class,
                () -> e.cambiarEstado.ejecutar(accion(AccionPublicacion.CANCELAR, publicacion, "motivo")));
        assertEquals("PUBLICADA", e.publicaciones.obtenerPorId(publicacion).getNombreEstado());
    }

    @Test
    void cerrarRechazaLasSolicitudesPendientesYAvisaAlCliente() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista pendiente = e.solicitarFranja(publicacion, F1);

        e.cambiarEstado.ejecutar(accion(AccionPublicacion.CERRAR, publicacion, null));

        assertEquals("RECHAZADA", e.contrataciones.obtenerPorId(pendiente.id()).getNombreEstado());
        var notificaciones = e.notificaciones.listarPorDestinatario(CLIENTE, SolicitudPagina.primera());
        assertEquals(1, notificaciones.total());
        assertEquals(TipoEvento.SOLICITUD_RECHAZADA, notificaciones.elementos().get(0).getTipo());
    }

    @Test
    void pausarNoAfectaLasSolicitudesPendientes() {
        Long publicacion = e.publicacionPublicada();
        ContratacionVista pendiente = e.solicitarFranja(publicacion, F1);

        e.cambiarEstado.ejecutar(accion(AccionPublicacion.PAUSAR, publicacion, null));

        assertEquals("PENDIENTE", e.contrataciones.obtenerPorId(pendiente.id()).getNombreEstado());
    }

    @Test
    void agregarPromocionLaSumaALaPublicacion() {
        Long publicacion = e.publicacionPublicada();

        PublicacionVista vista = e.agregarPromocion.ejecutar(new AgregarPromocionComando(PROVEEDOR, publicacion,
                TipoPromocion.PORCENTAJE, 10, Escenario.INICIO, Escenario.INICIO.plus(Duration.ofDays(7))));

        assertEquals(1, vista.promociones());
    }

    @Test
    void unaPromocionPorcentualFueraDeRangoSeRechaza() {
        Long publicacion = e.publicacionPublicada();

        assertThrows(ValorInvalido.class, () -> e.agregarPromocion.ejecutar(new AgregarPromocionComando(PROVEEDOR,
                publicacion, TipoPromocion.PORCENTAJE, 150, Escenario.INICIO, Escenario.INICIO.plusSeconds(60))));
    }

    @Test
    void soloElAutorAgregaPromociones() {
        Long publicacion = e.publicacionPublicada();

        assertThrows(PermisoDenegado.class, () -> e.agregarPromocion.ejecutar(new AgregarPromocionComando(CLIENTE,
                publicacion, TipoPromocion.MONTO_FIJO, 5, Escenario.INICIO, Escenario.INICIO.plusSeconds(60))));
    }

    @Test
    void unaPublicacionInexistenteSeInformaComoRecursoNoEncontrado() {
        assertThrows(RecursoNoEncontrado.class,
                () -> e.cambiarEstado.ejecutar(accion(AccionPublicacion.PUBLICAR, 404L, null)));
    }
}
