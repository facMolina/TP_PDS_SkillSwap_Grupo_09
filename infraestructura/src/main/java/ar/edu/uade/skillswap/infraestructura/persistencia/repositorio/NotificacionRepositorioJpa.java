package ar.edu.uade.skillswap.infraestructura.persistencia.repositorio;

import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.puerto.NotificacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.NotificacionEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.mapeo.NotificacionMapeador;

public class NotificacionRepositorioJpa extends RepositorioJpa implements NotificacionRepositorio {

    public NotificacionRepositorioJpa(ContextoPersistencia contexto) {
        super(contexto);
    }

    @Override
    public Notificacion guardar(Notificacion notificacion) {
        NotificacionEntidad entidad;
        if (notificacion.getId() == null) {
            entidad = NotificacionMapeador.nueva(notificacion);
            NotificacionMapeador.copiar(notificacion, entidad);
            entityManager().persist(entidad);
        } else {
            entidad = entityManager().find(NotificacionEntidad.class, notificacion.getId());
            NotificacionMapeador.copiar(notificacion, entidad);
        }
        sincronizar();
        notificacion.asignarId(entidad.id);
        return notificacion;
    }

    @Override
    public Optional<Notificacion> buscarPorId(Long id) {
        return Optional.ofNullable(entityManager().find(NotificacionEntidad.class, id))
                .map(NotificacionMapeador::aDominio);
    }

    @Override
    public Pagina<Notificacion> listarPorDestinatario(UsuarioId usuario, SolicitudPagina pagina) {
        Long total = entityManager()
                .createQuery("select count(n) from NotificacionEntidad n where n.destinatarioId = :usuario",
                        Long.class)
                .setParameter("usuario", usuario.valor())
                .getSingleResult();
        var notificaciones = entityManager()
                .createQuery("select n from NotificacionEntidad n where n.destinatarioId = :usuario "
                        + "order by n.fecha desc, n.id desc", NotificacionEntidad.class)
                .setParameter("usuario", usuario.valor())
                .setFirstResult(pagina.desplazamiento())
                .setMaxResults(pagina.tamanio())
                .getResultList()
                .stream()
                .map(NotificacionMapeador::aDominio)
                .toList();
        return new Pagina<>(notificaciones, pagina.numero(), pagina.tamanio(), total);
    }
}
