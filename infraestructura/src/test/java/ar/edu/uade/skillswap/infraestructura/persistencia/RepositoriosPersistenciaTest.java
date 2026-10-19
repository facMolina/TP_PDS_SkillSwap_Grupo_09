package ar.edu.uade.skillswap.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ReprogramarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.ActualizarPerfilComando;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.CrearPerfilComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AgregarPromocionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.TipoPromocion;
import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.dominio.billetera.MovimientoCreditos;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.perfil.CanalPreferido;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.infraestructura.soporte.PruebaConBaseDatos;

class RepositoriosPersistenciaTest extends PruebaConBaseDatos {

    private <T> T enTransaccion(Supplier<T> consulta) {
        return app.aplicacion.transaccion.consultar(consulta);
    }

    @Test
    void elPerfilConservaHabilidadesInteresesDisponibilidadYCanal() {
        registrar(PROVEEDOR, TipoPerfil.PERSONA);
        FranjaHoraria disponible = franja("2026-11-20T10:00:00Z", "2026-11-20T12:00:00Z");
        app.aplicacion.actualizarPerfil.ejecutar(new ActualizarPerfilComando(PROVEEDOR, "Ana", "Biografía",
                ZoneId.of("America/Argentina/Buenos_Aires"), Set.of(JAVA, SQL), Set.of(17L), List.of(disponible),
                CanalPreferido.PUSH));

        Perfil perfil = enTransaccion(() -> app.puertos.perfiles().obtenerPorUsuario(PROVEEDOR));

        assertEquals("Ana", perfil.getNombreVisible());
        assertEquals(ZoneId.of("America/Argentina/Buenos_Aires"), perfil.getZonaHoraria());
        assertEquals(Set.of(new Habilidad(JAVA, "Java"), new Habilidad(SQL, "SQL")), perfil.getHabilidades());
        assertEquals(Set.of(new Habilidad(17L, "Inglés")), perfil.getIntereses());
        assertEquals(List.of(disponible), perfil.getDisponibilidad());
        assertEquals(CanalPreferido.PUSH, perfil.getCanalPreferido());
        assertTrue(enTransaccion(() -> app.puertos.perfiles().existePorUsuario(PROVEEDOR)));
        assertFalse(enTransaccion(() -> app.puertos.perfiles().existePorUsuario(CLIENTE)));
    }

    @Test
    void laActualizacionDeHabilidadesReemplazaElConjuntoAnterior() {
        registrar(PROVEEDOR, TipoPerfil.PERSONA);
        actualizarHabilidades(Set.of(JAVA, SQL));
        actualizarHabilidades(Set.of(SQL));

        Perfil perfil = enTransaccion(() -> app.puertos.perfiles().obtenerPorUsuario(PROVEEDOR));

        assertEquals(Set.of(new Habilidad(SQL, "SQL")), perfil.getHabilidades());
    }

    @Test
    void laBilleteraConservaSaldoYMovimientosEnOrden() {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(3);
        ContratacionVista contratacion = solicitar(CLIENTE, publicacion, F1);
        aceptar(contratacion.id());
        pagar(CLIENTE, contratacion.id());

        Pagina<MovimientoCreditos> movimientos =
                enTransaccion(() -> app.puertos.billeteras().listarMovimientos(CLIENTE, new SolicitudPagina(0, 1)));

        assertEquals(2, movimientos.total());
        assertEquals(1, movimientos.elementos().size());
        assertEquals(2, movimientos.totalPaginas());
        assertEquals(SkillCoins.de(900),
                enTransaccion(() -> app.puertos.billeteras().obtenerPorUsuario(CLIENTE)).getSaldo());
    }

    @Test
    void laPublicacionConservaFranjasPromocionesYHistorial() {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(2);
        Instant desde = inicio().minusSeconds(3600);
        Instant hasta = inicio().plusSeconds(86400);
        app.aplicacion.agregarPromocion.ejecutar(
                new AgregarPromocionComando(PROVEEDOR, publicacion, TipoPromocion.PORCENTAJE, 10, desde, hasta));
        app.aplicacion.agregarPromocion.ejecutar(
                new AgregarPromocionComando(PROVEEDOR, publicacion, TipoPromocion.MONTO_FIJO, 5, desde, hasta));
        ContratacionVista contratacion = solicitar(CLIENTE, publicacion, F2);
        aceptar(contratacion.id());

        Publicacion recuperada = enTransaccion(() -> app.puertos.publicaciones().obtenerPorId(publicacion));

        assertEquals(3, recuperada.getFranjas().size());
        assertTrue(recuperada.getFranjas().get(1).estaOcupada());
        assertFalse(recuperada.getFranjas().get(0).estaOcupada());
        assertEquals(2, recuperada.getPromociones().size());
        assertEquals(SkillCoins.de(85), recuperada.calcularPrecio(inicio()));
        assertEquals(1, recuperada.getCuposTomados());
        assertEquals("PUBLICADA", recuperada.getNombreEstado());
        assertEquals(List.of("BORRADOR", "PUBLICADA"),
                recuperada.getHistorial().getTransiciones().stream().map(t -> t.destino()).toList());
        assertEquals(Set.of(new Habilidad(JAVA, "Java")), recuperada.getEtiquetas());
        assertEquals(CATEGORIA_BACKEND, recuperada.getCategoria().id());
    }

    @Test
    void laContratacionConservaLaPropuestaDeReprogramacionPendiente() {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(3);
        ContratacionVista contratacion = solicitar(CLIENTE, publicacion, F1);
        aceptar(contratacion.id());
        app.aplicacion.proponerReprogramacion.ejecutar(new ReprogramarComando(CLIENTE, contratacion.id(), F3));

        var recuperada = enTransaccion(() -> app.puertos.contrataciones().obtenerPorId(contratacion.id()));

        assertEquals("ACEPTADA", recuperada.getNombreEstado());
        assertEquals(CLIENTE, recuperada.getPropuesta().getPropuestaPor());
        assertEquals(F3, recuperada.getPropuesta().getNuevaFranja());
        assertTrue(recuperada.getPropuesta().estaPendiente());
        assertEquals(F1, recuperada.getFranja());
        assertEquals(SkillCoins.de(100), recuperada.getPrecioAcordado());
    }

    @Test
    void lasConsultasDeContratacionesFiltranPorEstadoYParticipante() {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(3);
        ContratacionVista pendiente = solicitar(CLIENTE, publicacion, F1);
        ContratacionVista aceptada = solicitar(CLIENTE, publicacion, F2);
        aceptar(aceptada.id());

        assertEquals(List.of(pendiente.id()), enTransaccion(
                () -> app.puertos.contrataciones().buscarPendientesDePublicacion(publicacion)).stream()
                .map(c -> c.getId()).toList());
        assertTrue(enTransaccion(() -> app.puertos.contrataciones().existenComprometidasDe(CLIENTE)));
        assertTrue(enTransaccion(() -> app.puertos.contrataciones().existenComprometidasDePublicacion(publicacion)));
        assertEquals(1, enTransaccion(() -> app.puertos.contrataciones()
                .buscarAceptadasSinPagoDesde(inicio().plusSeconds(1))).size());
        assertEquals(0, enTransaccion(() -> app.puertos.contrataciones()
                .buscarAceptadasSinPagoDesde(inicio().minusSeconds(1))).size());
        assertTrue(enTransaccion(() -> app.puertos.contrataciones().historialResumido(CLIENTE, 5)).isEmpty());
    }

    @Test
    void elCatalogoSeCargaComoArbolConHabilidadesEnLasHojas() {
        Categoria programacion = enTransaccion(() -> app.puertos.catalogo().buscarCategoria(1L)).orElseThrow();

        assertTrue(programacion.idsIncluidos().containsAll(Set.of(1L, 2L, 3L, JAVA, 13L, 18L)));
        assertFalse(programacion.idsIncluidos().contains(SQL));
        assertEquals(Set.of(new Habilidad(JAVA, "Java")),
                enTransaccion(() -> app.puertos.catalogo().buscarHabilidades(Set.of(JAVA, 1L, 999L))));
        assertTrue(enTransaccion(() -> app.puertos.catalogo().buscarCategoria(JAVA)).isEmpty());
        assertTrue(enTransaccion(() -> app.puertos.catalogo().buscarHabilidades(Set.of())).isEmpty());
    }

    @Test
    void laUnidadDeTrabajoRevierteTodoSiLaAccionFalla() {
        assertThrows(IllegalStateException.class, () -> app.puertos.unidadDeTrabajo().ejecutar(() -> {
            app.aplicacion.crearPerfil.ejecutar(new CrearPerfilComando(
                    PROVEEDOR, TipoPerfil.PERSONA, "Ana", "ana@mail.com", ZoneId.of("UTC")));
            throw new IllegalStateException("falla posterior");
        }));

        assertFalse(enTransaccion(() -> app.puertos.perfiles().existePorUsuario(PROVEEDOR)));
        assertNull(enTransaccion(() -> app.puertos.billeteras().buscarPorUsuario(PROVEEDOR).orElse(null)));
    }

    @Test
    void sinUnidadDeTrabajoActivaLosRepositoriosRechazanLaOperacion() {
        assertThrows(IllegalStateException.class, () -> app.puertos.perfiles().existePorUsuario(PROVEEDOR));
    }

    private void actualizarHabilidades(Set<Long> habilidades) {
        app.aplicacion.actualizarPerfil.ejecutar(new ActualizarPerfilComando(PROVEEDOR, "Ana", null, ZoneId.of("UTC"),
                habilidades, Set.of(), List.of(), CanalPreferido.INTERNA));
    }
}
