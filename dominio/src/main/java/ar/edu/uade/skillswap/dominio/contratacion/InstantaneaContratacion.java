package ar.edu.uade.skillswap.dominio.contratacion;

import java.time.Instant;
import java.util.List;

import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.TransicionEstado;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record InstantaneaContratacion(Long id, Long publicacionId, UsuarioId clienteId, UsuarioId proveedorId,
                                      String mensaje, String objetivo, FranjaHoraria franja,
                                      SkillCoins precioAcordado, SkillCoins montoRetenido,
                                      TipoPoliticaCancelacion tipoPoliticaCancelacion, Instant fechaSolicitud,
                                      Instant fechaAceptacion, Instant fechaCompletada,
                                      PropuestaReprogramacion propuesta, String estado,
                                      List<TransicionEstado> historial) {
}
