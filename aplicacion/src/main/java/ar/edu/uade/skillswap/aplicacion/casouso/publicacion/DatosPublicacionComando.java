package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.util.Set;

import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;

public record DatosPublicacionComando(TipoPublicacion tipo, String titulo, String descripcion, Long categoriaId,
                                      Modalidad modalidad, int duracionMinutos, int cupo, SkillCoins precioBase,
                                      Set<Long> etiquetaIds, TipoPoliticaCancelacion tipoPoliticaCancelacion) {
}
