package ar.edu.uade.skillswap.infraestructura.persistencia.repositorio;

import java.util.List;
import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.PublicacionEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.mapeo.PublicacionMapeador;

public class PublicacionRepositorioJpa extends RepositorioJpa implements PublicacionRepositorio {

    public PublicacionRepositorioJpa(ContextoPersistencia contexto) {
        super(contexto);
    }

    @Override
    public Publicacion guardar(Publicacion publicacion) {
        PublicacionEntidad entidad;
        if (publicacion.getId() == null) {
            entidad = PublicacionMapeador.nueva(publicacion);
            PublicacionMapeador.copiar(publicacion, entidad, entityManager());
            entityManager().persist(entidad);
        } else {
            entidad = entityManager().find(PublicacionEntidad.class, publicacion.getId());
            PublicacionMapeador.copiar(publicacion, entidad, entityManager());
        }
        sincronizar();
        publicacion.asignarId(entidad.id);
        publicacion.fijarVersion(entidad.version);
        return publicacion;
    }

    @Override
    public Optional<Publicacion> buscarPorId(Long id) {
        return Optional.ofNullable(entityManager().find(PublicacionEntidad.class, id))
                .map(PublicacionMapeador::aDominio);
    }

    @Override
    public List<Publicacion> buscarVigentesPorAutor(UsuarioId autor) {
        return entityManager()
                .createQuery("select p from PublicacionEntidad p where p.autorId = :autor "
                        + "and p.estado in ('PUBLICADA', 'PAUSADA') order by p.id", PublicacionEntidad.class)
                .setParameter("autor", autor.valor())
                .getResultList()
                .stream()
                .map(PublicacionMapeador::aDominio)
                .toList();
    }
}
