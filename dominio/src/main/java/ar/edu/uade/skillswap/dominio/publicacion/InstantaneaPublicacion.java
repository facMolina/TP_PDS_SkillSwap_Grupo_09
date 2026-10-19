package ar.edu.uade.skillswap.dominio.publicacion;

import java.time.Instant;
import java.util.List;

import ar.edu.uade.skillswap.dominio.comun.TransicionEstado;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.precio.Promocion;

public record InstantaneaPublicacion(Long id, UsuarioId autorId, DatosPublicacion datos, int cuposTomados,
                                     List<FranjaDisponible> franjas, List<Promocion> promociones,
                                     String estado, List<TransicionEstado> historial, Instant fechaCreacion,
                                     long version) {
}
