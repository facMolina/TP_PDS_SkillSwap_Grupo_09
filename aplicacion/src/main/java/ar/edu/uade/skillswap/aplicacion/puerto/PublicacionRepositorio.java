package ar.edu.uade.skillswap.aplicacion.puerto;

import java.util.List;
import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public interface PublicacionRepositorio {

    Publicacion guardar(Publicacion publicacion);

    Optional<Publicacion> buscarPorId(Long id);

    List<Publicacion> buscarVigentesPorAutor(UsuarioId autor);

    default Publicacion obtenerPorId(Long id) {
        return buscarPorId(id).orElseThrow(() -> new RecursoNoEncontrado("No existe la publicación " + id));
    }
}
