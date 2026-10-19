package ar.edu.uade.skillswap.aplicacion.busqueda;

import java.util.Set;

import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;

public record FiltroBusqueda(String texto, Categoria categoria, Modalidad modalidad, SkillCoins precioMinimo,
                             SkillCoins precioMaximo, Habilidad habilidad, Double reputacionMinima) {

    public FiltroBusqueda {
        if (precioMinimo != null && precioMaximo != null && precioMaximo.esMenorQue(precioMinimo)) {
            throw new ValorInvalido("El precio máximo no puede ser menor al mínimo");
        }
        if (reputacionMinima != null && (reputacionMinima < 0 || reputacionMinima > 5)) {
            throw new ValorInvalido("La reputación mínima debe estar entre 0 y 5");
        }
    }

    public static FiltroBusqueda sinFiltros() {
        return new FiltroBusqueda(null, null, null, null, null, null, null);
    }

    public Set<Long> categoriasIncluidas() {
        return categoria == null ? Set.of() : categoria.idsIncluidos();
    }
}
