package ar.edu.uade.skillswap.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.busqueda.FiltroBusqueda;
import ar.edu.uade.skillswap.aplicacion.busqueda.OrdenBusqueda;
import ar.edu.uade.skillswap.aplicacion.busqueda.PublicacionResumen;
import ar.edu.uade.skillswap.aplicacion.casouso.busqueda.BusquedaConsulta;
import ar.edu.uade.skillswap.aplicacion.casouso.busqueda.RecomendacionConsulta;
import ar.edu.uade.skillswap.aplicacion.casouso.calificacion.CalificarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.ActualizarPerfilComando;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.DarDeBajaComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CambiarEstadoComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CrearBorradorComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.DatosPublicacionComando;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.perfil.CanalPreferido;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;
import ar.edu.uade.skillswap.infraestructura.soporte.PruebaConBaseDatos;

class BusquedaPersistenciaTest extends PruebaConBaseDatos {

    private static final long CATEGORIA_PROGRAMACION = 1L;
    private static final long CATEGORIA_DATOS = 4L;
    private static final long CATEGORIA_IDIOMAS = 6L;

    private Long java;
    private Long sql;
    private Long ingles;

    @BeforeEach
    void cargarOfertas() {
        registrarProveedorYCliente();
        java = publicar(PROVEEDOR, nueva("Mentoría Java avanzada", "Patrones y arquitectura", CATEGORIA_BACKEND, 120,
                Modalidad.REMOTA, Set.of(JAVA)), List.of(F1));
        sql = publicar(PROVEEDOR, nueva("Taller de consultas", "Aprendé SQL desde cero", CATEGORIA_DATOS, 50,
                Modalidad.PRESENCIAL, Set.of(SQL)), List.of(F2));
        ingles = publicar(PROVEEDOR, nueva("Conversación en inglés", "Práctica oral con Java como tema",
                CATEGORIA_IDIOMAS, 80, Modalidad.REMOTA, Set.of(17L)), List.of(F3));
        app.aplicacion.crearBorrador.ejecutar(new CrearBorradorComando(PROVEEDOR,
                nueva("Borrador oculto Java", "No publicado", CATEGORIA_BACKEND, 10, Modalidad.REMOTA, Set.of(JAVA)),
                List.of(F1)));
    }

    @Test
    void soloSeEncuentranLasPublicacionesPublicadas() {
        assertEquals(List.of(ingles, sql, java), ids(buscar(FiltroBusqueda.sinFiltros(), OrdenBusqueda.MAS_RECIENTE)));
    }

    @Test
    void laRelevanciaPondera3PorTitulo2PorEtiquetaY1PorDescripcion() {
        List<PublicacionResumen> resultados = buscar(filtroTexto("java"), OrdenBusqueda.RELEVANCIA);

        assertEquals(List.of(java, ingles), ids(resultados));
    }

    @Test
    void elTextoSinCoincidenciasNoDevuelveResultados() {
        assertTrue(buscar(filtroTexto("kotlin"), OrdenBusqueda.RELEVANCIA).isEmpty());
    }

    @Test
    void losCaracteresComodinNoSeInterpretanComoPatron() {
        assertTrue(buscar(filtroTexto("%"), OrdenBusqueda.RELEVANCIA).isEmpty());
    }

    @Test
    void laCategoriaIncluyeSusDescendientes() {
        Categoria programacion = categoria(CATEGORIA_PROGRAMACION);

        List<PublicacionResumen> resultados = buscar(
                new FiltroBusqueda(null, programacion, null, null, null, null, null), OrdenBusqueda.MAS_RECIENTE);

        assertEquals(List.of(java), ids(resultados));
    }

    @Test
    void sePuedeFiltrarPorModalidadPrecioYHabilidad() {
        assertEquals(List.of(sql), ids(buscar(
                new FiltroBusqueda(null, null, Modalidad.PRESENCIAL, null, null, null, null), OrdenBusqueda.MAS_RECIENTE)));
        assertEquals(List.of(ingles), ids(buscar(new FiltroBusqueda(null, null, null, SkillCoins.de(60),
                SkillCoins.de(100), null, null), OrdenBusqueda.MAS_RECIENTE)));
        assertEquals(List.of(java), ids(buscar(new FiltroBusqueda(null, null, null, null, null,
                new Habilidad(JAVA, "Java"), null), OrdenBusqueda.MAS_RECIENTE)));
    }

    @Test
    void elOrdenPorPrecioEsAscendente() {
        assertEquals(List.of(sql, ingles, java), ids(buscar(FiltroBusqueda.sinFiltros(), OrdenBusqueda.MENOR_PRECIO)));
    }

    @Test
    void laReputacionMinimaYElOrdenPorReputacionUsanElPromedioDelAutor() {
        assertTrue(buscar(new FiltroBusqueda(null, null, null, null, null, null, 4.0),
                OrdenBusqueda.MEJOR_REPUTACION).isEmpty());

        completarYCalificar(java, 5);

        List<PublicacionResumen> resultados = buscar(
                new FiltroBusqueda(null, null, null, null, null, null, 4.0), OrdenBusqueda.MEJOR_REPUTACION);

        assertEquals(3, resultados.size());
        assertEquals(5.0, resultados.get(0).reputacionAutor());
        assertEquals("Nombre proveedor", resultados.get(0).autor());
    }

    @Test
    void laPaginacionDevuelveElTotalYElTramoSolicitado() {
        var pagina = app.aplicacion.buscarPublicaciones.ejecutar(new BusquedaConsulta(FiltroBusqueda.sinFiltros(),
                OrdenBusqueda.MAS_RECIENTE, new SolicitudPagina(1, 2)));

        assertEquals(3, pagina.total());
        assertEquals(List.of(java), ids(pagina.elementos()));
        assertEquals(2, pagina.totalPaginas());
    }

    @Test
    void unaPublicacionPausadaOUnAutorDadoDeBajaDejaDeAparecer() {
        app.aplicacion.cambiarEstado.ejecutar(new CambiarEstadoComando(PROVEEDOR, sql, AccionPublicacion.PAUSAR, null));

        assertEquals(List.of(ingles, java), ids(buscar(FiltroBusqueda.sinFiltros(), OrdenBusqueda.MAS_RECIENTE)));

        app.aplicacion.darDeBajaPerfil.ejecutar(new DarDeBajaComando(PROVEEDOR));

        assertTrue(buscar(FiltroBusqueda.sinFiltros(), OrdenBusqueda.MAS_RECIENTE).isEmpty());
    }

    @Test
    void lasRecomendacionesPorAfinidadExcluyenLasPublicacionesPropias() {
        app.aplicacion.actualizarPerfil.ejecutar(new ActualizarPerfilComando(CLIENTE, "Cliente", null,
                ZoneId.of("UTC"), Set.of(), Set.of(JAVA), List.of(), CanalPreferido.INTERNA));

        var recomendadas = app.aplicacion.recomendarPublicaciones.ejecutar(new RecomendacionConsulta(CLIENTE, null));
        var propias = app.aplicacion.recomendarPublicaciones.ejecutar(new RecomendacionConsulta(PROVEEDOR, null));

        assertEquals(java, recomendadas.elementos().get(0).id());
        assertEquals(3, recomendadas.total());
        assertTrue(propias.elementos().isEmpty());
    }

    @Test
    void elPerfilPublicoListaLasPublicacionesVigentes() {
        var vista = app.aplicacion.consultarPerfilPublico.ejecutar(PROVEEDOR);

        assertEquals(List.of(java, sql, ingles), ids(vista.publicacionesVigentes()));
    }

    private DatosPublicacionComando nueva(String titulo, String descripcion, long categoria, long precio,
                                           Modalidad modalidad, Set<Long> etiquetas) {
        return new DatosPublicacionComando(TipoPublicacion.MENTORIA, titulo, descripcion, categoria, modalidad, 60, 3,
                SkillCoins.de(precio), etiquetas, TipoPoliticaCancelacion.FLEXIBLE);
    }

    private FiltroBusqueda filtroTexto(String texto) {
        return new FiltroBusqueda(texto, null, null, null, null, null, null);
    }

    private Categoria categoria(long id) {
        return app.aplicacion.transaccion.consultar(() -> app.puertos.catalogo().buscarCategoria(id)).orElseThrow();
    }

    private List<PublicacionResumen> buscar(FiltroBusqueda filtro, OrdenBusqueda orden) {
        return app.aplicacion.buscarPublicaciones
                .ejecutar(new BusquedaConsulta(filtro, orden, SolicitudPagina.primera())).elementos();
    }

    private List<Long> ids(List<PublicacionResumen> resumenes) {
        return resumenes.stream().map(PublicacionResumen::id).toList();
    }

    private void completarYCalificar(Long publicacion, int puntaje) {
        ContratacionVista contratacion = solicitar(CLIENTE, publicacion, F1);
        aceptar(contratacion.id());
        pagar(CLIENTE, contratacion.id());
        app.aplicacion.iniciar.ejecutar(new ContratacionComando(PROVEEDOR, contratacion.id()));
        app.aplicacion.completar.ejecutar(new ContratacionComando(PROVEEDOR, contratacion.id()));
        app.aplicacion.calificar.ejecutar(new CalificarComando(CLIENTE, contratacion.id(), new Puntaje(puntaje), "ok"));
    }
}
