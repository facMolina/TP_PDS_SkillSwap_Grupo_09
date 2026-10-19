package ar.edu.uade.skillswap.aplicacion.dobles;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

public class ContratacionRepositorioEnMemoria implements ContratacionRepositorio {

    private final Map<Long, Contratacion> almacen = new LinkedHashMap<>();
    private long secuencia;

    @Override
    public Contratacion guardar(Contratacion contratacion) {
        if (contratacion.getId() == null) {
            contratacion.asignarId(++secuencia);
        }
        almacen.put(contratacion.getId(), contratacion);
        return contratacion;
    }

    @Override
    public Optional<Contratacion> buscarPorId(Long id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public boolean existenComprometidasDe(UsuarioId usuario) {
        return almacen.values().stream()
                .anyMatch(contratacion -> contratacion.esParticipante(usuario) && contratacion.estaComprometida());
    }

    @Override
    public boolean existenComprometidasDePublicacion(Long publicacionId) {
        return almacen.values().stream()
                .anyMatch(contratacion -> contratacion.getPublicacionId().equals(publicacionId)
                        && contratacion.estaComprometida());
    }

    @Override
    public List<Contratacion> buscarPendientesDePublicacion(Long publicacionId) {
        return almacen.values().stream()
                .filter(contratacion -> contratacion.getPublicacionId().equals(publicacionId))
                .filter(contratacion -> contratacion.getNombreEstado().equals("PENDIENTE"))
                .toList();
    }

    @Override
    public List<Contratacion> buscarAceptadasSinPagoDesde(Instant limite) {
        return almacen.values().stream()
                .filter(contratacion -> contratacion.getNombreEstado().equals("ACEPTADA"))
                .filter(contratacion -> !contratacion.getFechaAceptacion().isAfter(limite))
                .toList();
    }

    @Override
    public List<Contratacion> historialResumido(UsuarioId usuario, int maximo) {
        return almacen.values().stream()
                .filter(contratacion -> contratacion.esParticipante(usuario) && contratacion.estaCompletada())
                .limit(maximo)
                .toList();
    }
}
