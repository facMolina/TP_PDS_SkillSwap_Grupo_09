package ar.edu.uade.skillswap.infraestructura.persistencia.repositorio;

import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.PerfilEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.mapeo.PerfilMapeador;

public class PerfilRepositorioJpa extends RepositorioJpa implements PerfilRepositorio {

    public PerfilRepositorioJpa(ContextoPersistencia contexto) {
        super(contexto);
    }

    @Override
    public Perfil guardar(Perfil perfil) {
        PerfilEntidad entidad;
        if (perfil.getId() == null) {
            entidad = PerfilMapeador.nueva(perfil);
            PerfilMapeador.copiar(perfil, entidad, entityManager());
            entityManager().persist(entidad);
        } else {
            entidad = entityManager().find(PerfilEntidad.class, perfil.getId());
            PerfilMapeador.copiar(perfil, entidad, entityManager());
        }
        sincronizar();
        perfil.asignarId(entidad.id);
        return perfil;
    }

    @Override
    public Optional<Perfil> buscarPorUsuario(UsuarioId usuario) {
        return entityManager()
                .createQuery("select p from PerfilEntidad p where p.usuarioId = :usuario", PerfilEntidad.class)
                .setParameter("usuario", usuario.valor())
                .getResultStream()
                .findFirst()
                .map(PerfilMapeador::aDominio);
    }

    @Override
    public boolean existePorUsuario(UsuarioId usuario) {
        Long cantidad = entityManager()
                .createQuery("select count(p) from PerfilEntidad p where p.usuarioId = :usuario", Long.class)
                .setParameter("usuario", usuario.valor())
                .getSingleResult();
        return cantidad > 0;
    }
}
