package ar.edu.uade.skillswap.aplicacion.casouso.busqueda;

import ar.edu.uade.skillswap.aplicacion.busqueda.PublicacionResumen;
import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.puerto.ConsultaPublicaciones;

public class BuscarPublicaciones {

    private final EjecutorTransaccional transaccion;
    private final ConsultaPublicaciones consulta;

    public BuscarPublicaciones(EjecutorTransaccional transaccion, ConsultaPublicaciones consulta) {
        this.transaccion = transaccion;
        this.consulta = consulta;
    }

    public Pagina<PublicacionResumen> ejecutar(BusquedaConsulta busqueda) {
        return transaccion.consultar(
                () -> consulta.buscar(busqueda.filtro(), busqueda.orden(), busqueda.pagina()));
    }
}
