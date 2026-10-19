package ar.edu.uade.skillswap.aplicacion.dobles;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ar.edu.uade.skillswap.aplicacion.error.ConflictoDeConcurrencia;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class PublicacionRepositorioEnMemoria implements PublicacionRepositorio {

    private final Map<Long, Publicacion> almacen = new LinkedHashMap<>();
    private long secuencia;
    private boolean conflictoProgramado;

    public void programarConflictoEnElProximoGuardado() {
        conflictoProgramado = true;
    }

    @Override
    public Publicacion guardar(Publicacion publicacion) {
        if (conflictoProgramado) {
            conflictoProgramado = false;
            throw new ConflictoDeConcurrencia("La publicación fue modificada por otra transacción");
        }
        if (publicacion.getId() == null) {
            publicacion.asignarId(++secuencia);
        }
        publicacion.fijarVersion(publicacion.getVersion() + 1);
        almacen.put(publicacion.getId(), publicacion);
        return publicacion;
    }

    @Override
    public Optional<Publicacion> buscarPorId(Long id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public List<Publicacion> buscarVigentesPorAutor(UsuarioId autor) {
        return almacen.values().stream()
                .filter(publicacion -> publicacion.getAutorId().equals(autor))
                .filter(publicacion -> Set.of("PUBLICADA", "PAUSADA").contains(publicacion.getNombreEstado()))
                .toList();
    }
}
