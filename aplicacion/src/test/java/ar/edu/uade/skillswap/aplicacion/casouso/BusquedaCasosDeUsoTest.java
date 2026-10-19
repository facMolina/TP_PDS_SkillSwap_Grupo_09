package ar.edu.uade.skillswap.aplicacion.casouso;

import static ar.edu.uade.skillswap.aplicacion.Escenario.CLIENTE;
import static ar.edu.uade.skillswap.aplicacion.Escenario.JAVA;
import static ar.edu.uade.skillswap.aplicacion.Escenario.SQL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.Escenario;
import ar.edu.uade.skillswap.aplicacion.busqueda.FiltroBusqueda;
import ar.edu.uade.skillswap.aplicacion.busqueda.OrdenBusqueda;
import ar.edu.uade.skillswap.aplicacion.busqueda.PublicacionResumen;
import ar.edu.uade.skillswap.aplicacion.casouso.busqueda.BusquedaConsulta;
import ar.edu.uade.skillswap.aplicacion.casouso.busqueda.RecomendacionConsulta;
import ar.edu.uade.skillswap.aplicacion.casouso.busqueda.RecomendarPublicaciones;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.ActualizarPerfilComando;
import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.recomendacion.CandidataRecomendacion;
import ar.edu.uade.skillswap.dominio.recomendacion.EstrategiaRecomendacion;
import ar.edu.uade.skillswap.dominio.recomendacion.RecomendacionPorAfinidad;
import ar.edu.uade.skillswap.dominio.recomendacion.RecomendacionPorPopularidad;

class BusquedaCasosDeUsoTest {

    private Escenario e;

    @BeforeEach
    void preparar() {
        e = new Escenario().conPerfiles();
        e.actualizarPerfil.ejecutar(new ActualizarPerfilComando(CLIENTE, "Cliente", null, ZoneId.of("UTC"),
                Set.of(10L, 11L), null, null, null));
        List<CandidataRecomendacion> candidatas = List.of(
                new CandidataRecomendacion(1L, Set.of(JAVA), 1, 3.0),
                new CandidataRecomendacion(2L, Set.of(JAVA, SQL), 10, 4.0),
                new CandidataRecomendacion(3L, Set.of(), 100, 5.0));
        when(e.consulta.candidatasParaRecomendar(any())).thenReturn(candidatas);
        when(e.consulta.resumenesPorIds(anyList())).thenAnswer(invocacion -> {
            List<Long> ids = invocacion.getArgument(0);
            return ids.reversed().stream().map(this::resumen).toList();
        });
    }

    private PublicacionResumen resumen(Long id) {
        return new PublicacionResumen(id, "Publicación " + id, "Autor", SkillCoins.de(100), 0);
    }

    private RecomendarPublicaciones recomendador(EstrategiaRecomendacion estrategia) {
        return new RecomendarPublicaciones(e.transaccion, e.perfiles, e.consulta, estrategia);
    }

    private List<Long> ids(Pagina<PublicacionResumen> pagina) {
        return pagina.elementos().stream().map(PublicacionResumen::id).toList();
    }

    @Test
    void buscarDelegaElFiltroElOrdenYLaPagina() {
        FiltroBusqueda filtro = new FiltroBusqueda("java", null, Modalidad.REMOTA, SkillCoins.de(10),
                SkillCoins.de(200), JAVA, 3.5);
        SolicitudPagina pagina = new SolicitudPagina(2, 5);
        Pagina<PublicacionResumen> esperada = new Pagina<>(List.of(resumen(1L)), 2, 5, 11);
        when(e.consulta.buscar(filtro, OrdenBusqueda.MENOR_PRECIO, pagina)).thenReturn(esperada);

        Pagina<PublicacionResumen> resultado =
                e.buscarPublicaciones.ejecutar(new BusquedaConsulta(filtro, OrdenBusqueda.MENOR_PRECIO, pagina));

        assertEquals(esperada, resultado);
        assertEquals(3, resultado.totalPaginas());
    }

    @Test
    void sinParametrosUsaLosValoresPorDefecto() {
        e.buscarPublicaciones.ejecutar(new BusquedaConsulta(null, null, null));

        verify(e.consulta).buscar(eq(FiltroBusqueda.sinFiltros()), eq(OrdenBusqueda.RELEVANCIA),
                eq(SolicitudPagina.primera()));
        assertEquals(10, SolicitudPagina.primera().tamanio());
    }

    @Test
    void elFiltroExigeRangosCoherentes() {
        assertThrows(ValorInvalido.class, () -> new FiltroBusqueda(null, null, null, SkillCoins.de(50),
                SkillCoins.de(10), null, null));
        assertThrows(ValorInvalido.class, () -> new FiltroBusqueda(null, null, null, null, null, null, 6.0));
    }

    @Test
    void filtrarPorUnaCategoriaIncluyeSusSubcategorias() {
        Categoria padre = new Categoria(100L, "Tecnología");
        Categoria hija = new Categoria(101L, "Datos");
        padre.agregar(hija);
        hija.agregar(SQL);

        FiltroBusqueda filtro = new FiltroBusqueda(null, padre, null, null, null, null, null);

        assertEquals(Set.of(100L, 101L, 11L), filtro.categoriasIncluidas());
        assertEquals(Set.of(), FiltroBusqueda.sinFiltros().categoriasIncluidas());
    }

    @Test
    void laRecomendacionPorAfinidadOrdenaPorEtiquetasEnComun() {
        Pagina<PublicacionResumen> resultado =
                recomendador(new RecomendacionPorAfinidad()).ejecutar(new RecomendacionConsulta(CLIENTE, null));

        assertEquals(List.of(2L, 1L, 3L), ids(resultado));
    }

    @Test
    void cambiarLaEstrategiaNoModificaElCasoDeUso() {
        RecomendacionConsulta consulta = new RecomendacionConsulta(CLIENTE, null);

        List<Long> porAfinidad = ids(recomendador(new RecomendacionPorAfinidad()).ejecutar(consulta));
        List<Long> porPopularidad = ids(recomendador(new RecomendacionPorPopularidad()).ejecutar(consulta));

        assertEquals(List.of(2L, 1L, 3L), porAfinidad);
        assertEquals(List.of(3L, 2L, 1L), porPopularidad);
    }

    @Test
    void elDesempateEsLaReputacionDelAutor() {
        when(e.consulta.candidatasParaRecomendar(any())).thenReturn(List.of(
                new CandidataRecomendacion(7L, Set.of(JAVA), 0, 2.0),
                new CandidataRecomendacion(8L, Set.of(JAVA), 0, 4.5),
                new CandidataRecomendacion(9L, Set.of(JAVA), 0, 4.5)));

        Pagina<PublicacionResumen> resultado =
                recomendador(new RecomendacionPorAfinidad()).ejecutar(new RecomendacionConsulta(CLIENTE, null));

        assertEquals(List.of(8L, 9L, 7L), ids(resultado));
    }

    @Test
    void laRecomendacionSePagina() {
        RecomendarPublicaciones recomendador = recomendador(new RecomendacionPorAfinidad());

        Pagina<PublicacionResumen> primera =
                recomendador.ejecutar(new RecomendacionConsulta(CLIENTE, new SolicitudPagina(0, 2)));
        Pagina<PublicacionResumen> segunda =
                recomendador.ejecutar(new RecomendacionConsulta(CLIENTE, new SolicitudPagina(1, 2)));

        assertEquals(List.of(2L, 1L), ids(primera));
        assertEquals(List.of(3L), ids(segunda));
        assertEquals(3, primera.total());
        assertEquals(2, primera.totalPaginas());
    }

    @Test
    void recomendarParaUnPerfilInexistenteFalla() {
        assertThrows(RecursoNoEncontrado.class, () -> recomendador(new RecomendacionPorAfinidad())
                .ejecutar(new RecomendacionConsulta(new UsuarioId("nadie"), null)));
    }
}
