package ar.edu.uade.skillswap.aplicacion.comun;

import java.util.function.Supplier;

import ar.edu.uade.skillswap.aplicacion.puerto.PublicadorEventos;
import ar.edu.uade.skillswap.aplicacion.puerto.UnidadDeTrabajo;

public class EjecutorTransaccional {

    private final UnidadDeTrabajo unidadDeTrabajo;
    private final PublicadorEventos publicador;

    public EjecutorTransaccional(UnidadDeTrabajo unidadDeTrabajo, PublicadorEventos publicador) {
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.publicador = publicador;
    }

    public <T> T ejecutar(Supplier<ResultadoTransaccion<T>> accion) {
        ResultadoTransaccion<T> resultado = unidadDeTrabajo.ejecutar(accion);
        resultado.eventos().forEach(publicador::publicar);
        return resultado.valor();
    }

    public <T> T consultar(Supplier<T> consulta) {
        return unidadDeTrabajo.ejecutar(consulta);
    }
}
