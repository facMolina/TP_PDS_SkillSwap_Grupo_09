package ar.edu.uade.skillswap.aplicacion.busqueda;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public record PublicacionResumen(Long id, String titulo, String autor, SkillCoins precio, double reputacionAutor) {
}
