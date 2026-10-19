package ar.edu.uade.skillswap.aplicacion.puerto;

import java.util.function.Supplier;

public interface UnidadDeTrabajo {

    <T> T ejecutar(Supplier<T> accion);
}
