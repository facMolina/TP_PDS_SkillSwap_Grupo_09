package ar.edu.uade.skillswap.aplicacion.puerto;

import ar.edu.uade.skillswap.dominio.calificacion.Calificacion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public interface CalificacionRepositorio {

    Calificacion guardar(Calificacion calificacion);

    boolean existe(Long contratacionId, UsuarioId autor);
}
