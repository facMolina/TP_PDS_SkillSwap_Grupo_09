package ar.edu.uade.skillswap.infraestructura.persistencia.repositorio;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.ContratacionEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.mapeo.ContratacionMapeador;

public class ContratacionRepositorioJpa extends RepositorioJpa implements ContratacionRepositorio {

    private static final String COMPROMETIDAS = "('ACEPTADA', 'CONFIRMADA', 'EN_CURSO')";

    public ContratacionRepositorioJpa(ContextoPersistencia contexto) {
        super(contexto);
    }

    @Override
    public Contratacion guardar(Contratacion contratacion) {
        ContratacionEntidad entidad;
        if (contratacion.getId() == null) {
            entidad = ContratacionMapeador.nueva(contratacion);
            ContratacionMapeador.copiar(contratacion, entidad);
            entityManager().persist(entidad);
        } else {
            entidad = entityManager().find(ContratacionEntidad.class, contratacion.getId());
            ContratacionMapeador.copiar(contratacion, entidad);
        }
        sincronizar();
        contratacion.asignarId(entidad.id);
        return contratacion;
    }

    @Override
    public Optional<Contratacion> buscarPorId(Long id) {
        return Optional.ofNullable(entityManager().find(ContratacionEntidad.class, id))
                .map(ContratacionMapeador::aDominio);
    }

    @Override
    public boolean existenComprometidasDe(UsuarioId usuario) {
        return contar("select count(c) from ContratacionEntidad c where (c.clienteId = :usuario "
                + "or c.proveedorId = :usuario) and c.estado in " + COMPROMETIDAS, "usuario", usuario.valor()) > 0;
    }

    @Override
    public boolean existenComprometidasDePublicacion(Long publicacionId) {
        return contar("select count(c) from ContratacionEntidad c where c.publicacionId = :publicacion "
                + "and c.estado in " + COMPROMETIDAS, "publicacion", publicacionId) > 0;
    }

    @Override
    public List<Contratacion> buscarPendientesDePublicacion(Long publicacionId) {
        return entityManager()
                .createQuery("select c from ContratacionEntidad c where c.publicacionId = :publicacion "
                        + "and c.estado = 'PENDIENTE' order by c.id", ContratacionEntidad.class)
                .setParameter("publicacion", publicacionId)
                .getResultList()
                .stream()
                .map(ContratacionMapeador::aDominio)
                .toList();
    }

    @Override
    public List<Contratacion> buscarAceptadasSinPagoDesde(Instant limite) {
        return entityManager()
                .createQuery("select c from ContratacionEntidad c where c.estado = 'ACEPTADA' "
                        + "and c.fechaAceptacion <= :limite order by c.id", ContratacionEntidad.class)
                .setParameter("limite", limite)
                .getResultList()
                .stream()
                .map(ContratacionMapeador::aDominio)
                .toList();
    }

    @Override
    public List<Contratacion> historialResumido(UsuarioId usuario, int maximo) {
        return entityManager()
                .createQuery("select c from ContratacionEntidad c where (c.clienteId = :usuario "
                        + "or c.proveedorId = :usuario) and c.estado = 'COMPLETADA' "
                        + "order by c.fechaCompletada desc, c.id desc", ContratacionEntidad.class)
                .setParameter("usuario", usuario.valor())
                .setMaxResults(maximo)
                .getResultList()
                .stream()
                .map(ContratacionMapeador::aDominio)
                .toList();
    }

    private long contar(String consulta, String parametro, Object valor) {
        return entityManager().createQuery(consulta, Long.class).setParameter(parametro, valor).getSingleResult();
    }
}
