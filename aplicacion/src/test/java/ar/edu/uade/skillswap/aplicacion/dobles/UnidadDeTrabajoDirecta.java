package ar.edu.uade.skillswap.aplicacion.dobles;

import java.util.function.Supplier;

import ar.edu.uade.skillswap.aplicacion.puerto.UnidadDeTrabajo;

public class UnidadDeTrabajoDirecta implements UnidadDeTrabajo {

    @Override
    public <T> T ejecutar(Supplier<T> accion) {
        return accion.get();
    }
}
