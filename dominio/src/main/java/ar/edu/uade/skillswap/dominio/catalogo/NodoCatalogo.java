package ar.edu.uade.skillswap.dominio.catalogo;

import java.util.Set;

public interface NodoCatalogo {

    Long id();

    String nombre();

    boolean incluye(NodoCatalogo nodo);

    Set<Long> idsIncluidos();
}
