package ar.edu.uade.skillswap.infraestructura.persistencia.repositorio;

import ar.edu.uade.skillswap.aplicacion.puerto.CalificacionRepositorio;
import ar.edu.uade.skillswap.dominio.calificacion.Calificacion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.CalificacionEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.mapeo.CalificacionMapeador;

public class CalificacionRepositorioJpa extends RepositorioJpa implements CalificacionRepositorio {

    public CalificacionRepositorioJpa(ContextoPersistencia contexto) {
        super(contexto);
    }

    @Override
    public Calificacion guardar(Calificacion calificacion) {
        CalificacionEntidad entidad = CalificacionMapeador.aEntidad(calificacion);
        entityManager().persist(entidad);
        sincronizar();
        calificacion.asignarId(entidad.id);
        return calificacion;
    }

    @Override
    public boolean existe(Long contratacionId, UsuarioId autor) {
        Long cantidad = entityManager()
                .createQuery("select count(c) from CalificacionEntidad c where c.contratacionId = :contratacion "
                        + "and c.autorId = :autor", Long.class)
                .setParameter("contratacion", contratacionId)
                .setParameter("autor", autor.valor())
                .getSingleResult();
        return cantidad > 0;
    }
}
