package ar.edu.uade.skillswap.aplicacion.casouso.busqueda;

import ar.edu.uade.skillswap.aplicacion.busqueda.FiltroBusqueda;
import ar.edu.uade.skillswap.aplicacion.busqueda.OrdenBusqueda;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;

public record BusquedaConsulta(FiltroBusqueda filtro, OrdenBusqueda orden, SolicitudPagina pagina) {

    public BusquedaConsulta {
        filtro = filtro == null ? FiltroBusqueda.sinFiltros() : filtro;
        orden = orden == null ? OrdenBusqueda.RELEVANCIA : orden;
        pagina = pagina == null ? SolicitudPagina.primera() : pagina;
    }
}
