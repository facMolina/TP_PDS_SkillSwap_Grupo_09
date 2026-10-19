package ar.edu.uade.skillswap.dominio.publicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.Fixtures;
import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.error.CupoAgotado;
import ar.edu.uade.skillswap.dominio.error.FranjaNoDisponible;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;
import ar.edu.uade.skillswap.dominio.error.PublicacionIncompleta;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.precio.PromocionPorcentual;

class PublicacionTest {

    private static DatosPublicacion datos(TipoPublicacion tipo) {
        return new DatosPublicacion(tipo, "Título", "Descripción", Fixtures.CATEGORIA, Modalidad.REMOTA, 60, 1,
                SkillCoins.de(10), Set.of(), null);
    }

    @Test
    void nacePorDefectoEnBorradorConPoliticaFlexible() {
        Publicacion publicacion = Fixtures.borrador();

        assertEquals("BORRADOR", publicacion.getNombreEstado());
        assertEquals(TipoPoliticaCancelacion.FLEXIBLE, publicacion.getTipoPoliticaCancelacion());
        assertEquals(1, publicacion.getHistorial().getTransiciones().size());
    }

    @Test
    void soloElPerfilHabilitadoPuedeCrearElTipoDePublicacion() {
        assertThrows(PermisoDenegado.class,
                () -> Publicacion.crearBorrador(Fixtures.perfilProveedor(), datos(TipoPublicacion.DESAFIO), Fixtures.AHORA));
        assertThrows(PermisoDenegado.class,
                () -> Publicacion.crearBorrador(Fixtures.perfilOrganizacion(), datos(TipoPublicacion.MENTORIA), Fixtures.AHORA));
        assertEquals(TipoPublicacion.DESAFIO, Publicacion
                .crearBorrador(Fixtures.perfilOrganizacion(), datos(TipoPublicacion.DESAFIO), Fixtures.AHORA).getTipo());
    }

    @Test
    void elTipoEsObligatorio() {
        assertThrows(ValorInvalido.class,
                () -> Publicacion.crearBorrador(Fixtures.perfilProveedor(), datos(null), Fixtures.AHORA));
    }

    @Test
    void unaOfertaIncompletaNoSePuedePublicar() {
        Publicacion sinFranjas = Fixtures.borrador();
        Publicacion sinTitulo = Publicacion.crearBorrador(Fixtures.perfilProveedor(),
                new DatosPublicacion(TipoPublicacion.MENTORIA, " ", null, null, Modalidad.REMOTA, 60, 1, null, null, null),
                Fixtures.AHORA);

        assertThrows(PublicacionIncompleta.class, () -> sinFranjas.publicar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        PublicacionIncompleta error =
                assertThrows(PublicacionIncompleta.class, () -> sinTitulo.publicar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertTrue(error.getMessage().contains("título"));
        assertTrue(error.getMessage().contains("categoría"));
        assertTrue(error.getMessage().contains("precio"));
        assertTrue(error.getMessage().contains("franja"));
        assertEquals("BORRADOR", sinTitulo.getNombreEstado());
    }

    @Test
    void elBorradorEsEditableYLuegoNo() {
        Publicacion publicacion = Fixtures.publicacionPublicada();

        assertThrows(TransicionInvalida.class,
                () -> publicacion.editar(Fixtures.datosCompletos(3, SkillCoins.de(5)), Fixtures.ctx(Fixtures.PROVEEDOR)));

        Publicacion borrador = Fixtures.borrador();
        borrador.editar(Fixtures.datosCompletos(3, SkillCoins.de(5)), Fixtures.ctx(Fixtures.PROVEEDOR));
        assertEquals(3, borrador.getCupo());
        assertEquals(SkillCoins.de(5), borrador.getPrecioBase());
    }

    @Test
    void noSePuedeCambiarElTipoAlEditar() {
        Publicacion borrador = Fixtures.borrador();

        assertThrows(ValorInvalido.class,
                () -> borrador.editar(datos(TipoPublicacion.SERVICIO), Fixtures.ctx(Fixtures.PROVEEDOR)));
    }

    @Test
    void soloElAutorPuedeModificarLaPublicacion() {
        Publicacion publicacion = Fixtures.borrador();

        assertThrows(PermisoDenegado.class, () -> publicacion.publicar(Fixtures.ctx(Fixtures.CLIENTE)));
        assertThrows(PermisoDenegado.class,
                () -> publicacion.editar(Fixtures.datosCompletos(1, SkillCoins.de(1)), Fixtures.ctx(Fixtures.CLIENTE)));
        assertThrows(PermisoDenegado.class, () -> publicacion.agregarFranja(Fixtures.FRANJA, Fixtures.ctx(Fixtures.CLIENTE)));
    }

    @Test
    void recorreElCicloDeVidaPermitido() {
        Publicacion publicacion = Fixtures.publicacionPublicada();
        assertEquals("PUBLICADA", publicacion.getNombreEstado());
        assertTrue(publicacion.admiteSolicitudes());

        publicacion.pausar(Fixtures.ctx(Fixtures.PROVEEDOR));
        assertEquals("PAUSADA", publicacion.getNombreEstado());
        assertFalse(publicacion.admiteSolicitudes());

        publicacion.reanudar(Fixtures.ctx(Fixtures.PROVEEDOR));
        publicacion.cerrar(Fixtures.ctx(Fixtures.PROVEEDOR));
        assertEquals("CERRADA", publicacion.getNombreEstado());
        assertEquals(5, publicacion.getHistorial().getTransiciones().size());
    }

    @Test
    void unaPublicacionCerradaNoPuedeReabrirse() {
        Publicacion publicacion = Fixtures.publicacionPublicada();
        publicacion.cerrar(Fixtures.ctx(Fixtures.PROVEEDOR));

        assertThrows(TransicionInvalida.class, () -> publicacion.publicar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> publicacion.reanudar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> publicacion.pausar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> publicacion.cancelar(Fixtures.ctx(Fixtures.PROVEEDOR), "motivo"));
    }

    @Test
    void transicionesNoPermitidasDesdeBorradorYPublicada() {
        Publicacion borrador = Fixtures.borrador();
        Publicacion publicada = Fixtures.publicacionPublicada();

        assertThrows(TransicionInvalida.class, () -> borrador.pausar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> borrador.cerrar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> publicada.publicar(Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> publicada.reanudar(Fixtures.ctx(Fixtures.PROVEEDOR)));
    }

    @Test
    void cancelarExigeUnMotivoYRegistraloEnElHistorial() {
        Publicacion publicacion = Fixtures.publicacionPublicada();

        assertThrows(ValorInvalido.class, () -> publicacion.cancelar(Fixtures.ctx(Fixtures.PROVEEDOR), " "));

        publicacion.cancelar(Fixtures.ctx(Fixtures.PROVEEDOR), "Ya no ofrezco el servicio");

        var ultima = publicacion.getHistorial().getTransiciones().get(2);
        assertEquals("CANCELADA", publicacion.getNombreEstado());
        assertEquals("PUBLICADA", ultima.origen());
        assertEquals("Ya no ofrezco el servicio", ultima.motivo());
        assertEquals(Fixtures.PROVEEDOR, ultima.actorId());
    }

    @Test
    void sePuedeCancelarDesdePausada() {
        Publicacion publicacion = Fixtures.publicacionPublicada();
        publicacion.pausar(Fixtures.ctx(Fixtures.PROVEEDOR));

        publicacion.cancelar(Fixtures.ctx(Fixtures.PROVEEDOR), "Motivo");

        assertEquals("CANCELADA", publicacion.getNombreEstado());
    }

    @Test
    void lasPublicacionesNoPublicadasSoloLasVeSuAutor() {
        Publicacion borrador = Fixtures.borrador();
        Publicacion publicada = Fixtures.publicacionPublicada();

        assertTrue(borrador.esVisiblePara(Fixtures.PROVEEDOR));
        assertFalse(borrador.esVisiblePara(Fixtures.CLIENTE));
        assertTrue(publicada.esVisiblePara(Fixtures.CLIENTE));

        publicada.pausar(Fixtures.ctx(Fixtures.PROVEEDOR));
        assertTrue(publicada.esVisiblePara(Fixtures.PROVEEDOR));
        assertFalse(publicada.esVisiblePara(Fixtures.CLIENTE));
    }

    @Test
    void noSeAceptanFranjasSuperpuestas() {
        Publicacion publicacion = Fixtures.borrador();
        publicacion.agregarFranja(Fixtures.FRANJA, Fixtures.ctx(Fixtures.PROVEEDOR));

        assertThrows(ValorInvalido.class, () -> publicacion.agregarFranja(
                Fixtures.franja("2026-11-10T15:30:00Z", "2026-11-10T16:30:00Z"), Fixtures.ctx(Fixtures.PROVEEDOR)));
    }

    @Test
    void unaPublicacionFinalizadaNoAdmiteFranjasNiPromociones() {
        Publicacion publicacion = Fixtures.publicacionPublicada();
        publicacion.cerrar(Fixtures.ctx(Fixtures.PROVEEDOR));

        assertThrows(TransicionInvalida.class, () -> publicacion.agregarFranja(
                Fixtures.franja("2026-12-10T15:00:00Z", "2026-12-10T16:00:00Z"), Fixtures.ctx(Fixtures.PROVEEDOR)));
        assertThrows(TransicionInvalida.class, () -> publicacion.agregarPromocion(
                new PromocionPorcentual(10, Fixtures.AHORA, Fixtures.AHORA.plusSeconds(60)), Fixtures.ctx(Fixtures.PROVEEDOR)));
    }

    @Test
    void reservarCupoOcupaLaFranjaYConsumeElCupo() {
        Publicacion publicacion = Fixtures.publicacionPublicada();

        publicacion.reservarCupo(Fixtures.FRANJA);

        assertEquals(1, publicacion.getCuposTomados());
        assertFalse(publicacion.tieneFranjaLibre(Fixtures.FRANJA));
        assertTrue(publicacion.tieneFranjaLibre(Fixtures.OTRA_FRANJA));
    }

    @Test
    void noSePuedeReservarUnaFranjaOcupada() {
        Publicacion publicacion = Fixtures.publicacionPublicada();
        publicacion.reservarCupo(Fixtures.FRANJA);

        assertThrows(FranjaNoDisponible.class, () -> publicacion.reservarCupo(Fixtures.FRANJA));
        assertEquals(1, publicacion.getCuposTomados());
    }

    @Test
    void noSePuedeSuperarElCupo() {
        Publicacion publicacion = Fixtures.publicacionPublicada();
        publicacion.reservarCupo(Fixtures.FRANJA);
        publicacion.reservarCupo(Fixtures.OTRA_FRANJA);
        publicacion.agregarFranja(Fixtures.franja("2026-11-20T15:00:00Z", "2026-11-20T16:00:00Z"), Fixtures.ctx(Fixtures.PROVEEDOR));

        assertThrows(CupoAgotado.class,
                () -> publicacion.reservarCupo(Fixtures.franja("2026-11-20T15:00:00Z", "2026-11-20T16:00:00Z")));
    }

    @Test
    void liberarCupoDevuelveLaFranjaYElCupo() {
        Publicacion publicacion = Fixtures.publicacionPublicada();
        publicacion.reservarCupo(Fixtures.FRANJA);

        publicacion.liberarCupo(Fixtures.FRANJA);

        assertEquals(0, publicacion.getCuposTomados());
        assertTrue(publicacion.tieneFranjaLibre(Fixtures.FRANJA));
    }

    @Test
    void reservarUnaFranjaAjenaFalla() {
        Publicacion publicacion = Fixtures.publicacionPublicada();

        assertThrows(FranjaNoDisponible.class,
                () -> publicacion.reservarCupo(Fixtures.franja("2030-01-01T10:00:00Z", "2030-01-01T11:00:00Z")));
    }

    @Test
    void cambiarFranjaMueveLaReservaSinTocarElCupo() {
        Publicacion publicacion = Fixtures.publicacionPublicada();
        publicacion.reservarCupo(Fixtures.FRANJA);

        publicacion.cambiarFranja(Fixtures.FRANJA, Fixtures.OTRA_FRANJA);

        assertEquals(1, publicacion.getCuposTomados());
        assertTrue(publicacion.tieneFranjaLibre(Fixtures.FRANJA));
        assertFalse(publicacion.tieneFranjaLibre(Fixtures.OTRA_FRANJA));
    }

    @Test
    void laVersionSeConservaParaElBloqueoOptimista() {
        Publicacion publicacion = Fixtures.borrador();

        publicacion.fijarVersion(4);

        assertEquals(4, publicacion.getVersion());
    }
}
