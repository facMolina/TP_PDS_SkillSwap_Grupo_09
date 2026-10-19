package ar.edu.uade.skillswap.aplicacion.dobles;

import java.util.ArrayList;
import java.util.List;

import ar.edu.uade.skillswap.aplicacion.puerto.CalificacionRepositorio;
import ar.edu.uade.skillswap.dominio.calificacion.Calificacion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public class CalificacionRepositorioEnMemoria implements CalificacionRepositorio {

    private final List<Calificacion> almacen = new ArrayList<>();

    @Override
    public Calificacion guardar(Calificacion calificacion) {
        calificacion.asignarId((long) almacen.size() + 1);
        almacen.add(calificacion);
        return calificacion;
    }

    @Override
    public boolean existe(Long contratacionId, UsuarioId autor) {
        return almacen.stream().anyMatch(
                calificacion -> calificacion.getContratacionId().equals(contratacionId)
                        && calificacion.getAutorId().equals(autor));
    }

    public int cantidad() {
        return almacen.size();
    }
}
