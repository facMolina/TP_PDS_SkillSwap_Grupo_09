package ar.edu.uade.skillswap.infraestructura.persistencia.repositorio;

import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.puerto.BilleteraRepositorio;
import ar.edu.uade.skillswap.dominio.billetera.Billetera;
import ar.edu.uade.skillswap.dominio.billetera.MovimientoCreditos;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.BilleteraEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.MovimientoCreditoEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.mapeo.BilleteraMapeador;

public class BilleteraRepositorioJpa extends RepositorioJpa implements BilleteraRepositorio {

    public BilleteraRepositorioJpa(ContextoPersistencia contexto) {
        super(contexto);
    }

    @Override
    public Billetera guardar(Billetera billetera) {
        BilleteraEntidad entidad;
        if (billetera.getId() == null) {
            entidad = BilleteraMapeador.nueva(billetera);
            BilleteraMapeador.copiar(billetera, entidad);
            entityManager().persist(entidad);
        } else {
            entidad = entityManager().find(BilleteraEntidad.class, billetera.getId());
            BilleteraMapeador.copiar(billetera, entidad);
        }
        sincronizar();
        billetera.asignarId(entidad.id);
        billetera.fijarVersion(entidad.version);
        for (MovimientoCreditos movimiento : billetera.extraerMovimientos()) {
            entityManager().persist(BilleteraMapeador.aEntidad(movimiento, entidad.id));
        }
        sincronizar();
        return billetera;
    }

    @Override
    public Optional<Billetera> buscarPorUsuario(UsuarioId usuario) {
        return entityManager()
                .createQuery("select b from BilleteraEntidad b where b.usuarioId = :usuario", BilleteraEntidad.class)
                .setParameter("usuario", usuario.valor())
                .getResultStream()
                .findFirst()
                .map(BilleteraMapeador::aDominio);
    }

    @Override
    public Pagina<MovimientoCreditos> listarMovimientos(UsuarioId usuario, SolicitudPagina pagina) {
        String filtro = "from MovimientoCreditoEntidad m where m.billeteraId = "
                + "(select b.id from BilleteraEntidad b where b.usuarioId = :usuario)";
        Long total = entityManager().createQuery("select count(m) " + filtro, Long.class)
                .setParameter("usuario", usuario.valor())
                .getSingleResult();
        var movimientos = entityManager()
                .createQuery("select m " + filtro + " order by m.id", MovimientoCreditoEntidad.class)
                .setParameter("usuario", usuario.valor())
                .setFirstResult(pagina.desplazamiento())
                .setMaxResults(pagina.tamanio())
                .getResultList()
                .stream()
                .map(BilleteraMapeador::aDominio)
                .toList();
        return new Pagina<>(movimientos, pagina.numero(), pagina.tamanio(), total);
    }
}
