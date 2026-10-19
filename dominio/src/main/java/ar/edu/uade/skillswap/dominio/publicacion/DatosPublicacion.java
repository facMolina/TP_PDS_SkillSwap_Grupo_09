package ar.edu.uade.skillswap.dominio.publicacion;

import java.util.Set;

import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public record DatosPublicacion(TipoPublicacion tipo, String titulo, String descripcion, Categoria categoria,
                               Modalidad modalidad, int duracionMinutos, int cupo, SkillCoins precioBase,
                               Set<Habilidad> etiquetas, TipoPoliticaCancelacion tipoPoliticaCancelacion) {
}
