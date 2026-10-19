package ar.edu.uade.skillswap.dominio.recomendacion;

import java.util.Set;

import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;

public record CandidataRecomendacion(Long publicacionId, Set<Habilidad> etiquetas, int solicitudesRecientes,
                                     double reputacionAutor) {
}
