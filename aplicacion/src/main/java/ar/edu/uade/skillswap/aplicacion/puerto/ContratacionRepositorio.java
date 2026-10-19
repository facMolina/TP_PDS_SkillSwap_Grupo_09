package ar.edu.uade.skillswap.aplicacion.puerto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

public interface ContratacionRepositorio {

    Contratacion guardar(Contratacion contratacion);

    Optional<Contratacion> buscarPorId(Long id);

    boolean existenComprometidasDe(UsuarioId usuario);

    boolean existenComprometidasDePublicacion(Long publicacionId);

    List<Contratacion> buscarPendientesDePublicacion(Long publicacionId);

    List<Contratacion> buscarAceptadasSinPagoDesde(Instant limite);

    List<Contratacion> historialResumido(UsuarioId usuario, int maximo);

    default Contratacion obtenerPorId(Long id) {
        return buscarPorId(id).orElseThrow(() -> new RecursoNoEncontrado("No existe la contratación " + id));
    }
}
