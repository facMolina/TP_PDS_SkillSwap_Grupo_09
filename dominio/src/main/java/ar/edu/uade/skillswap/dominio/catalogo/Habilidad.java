package ar.edu.uade.skillswap.dominio.catalogo;

import java.util.Set;

public record Habilidad(Long id, String nombre) implements NodoCatalogo {

    @Override
    public boolean incluye(NodoCatalogo nodo) {
        return equals(nodo);
    }

    @Override
    public Set<Long> idsIncluidos() {
        return Set.of(id);
    }
}
