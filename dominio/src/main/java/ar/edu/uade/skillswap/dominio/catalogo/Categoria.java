package ar.edu.uade.skillswap.dominio.catalogo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Categoria implements NodoCatalogo {

    private final Long id;
    private final String nombre;
    private final List<NodoCatalogo> hijos = new ArrayList<>();

    public Categoria(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public void agregar(NodoCatalogo nodo) {
        hijos.add(nodo);
    }

    public void quitar(NodoCatalogo nodo) {
        hijos.remove(nodo);
    }

    public List<NodoCatalogo> hijos() {
        return Collections.unmodifiableList(hijos);
    }

    @Override
    public Long id() {
        return id;
    }

    @Override
    public String nombre() {
        return nombre;
    }

    @Override
    public boolean incluye(NodoCatalogo nodo) {
        return equals(nodo) || hijos.stream().anyMatch(hijo -> hijo.incluye(nodo));
    }

    @Override
    public Set<Long> idsIncluidos() {
        Set<Long> ids = new HashSet<>();
        ids.add(id);
        hijos.forEach(hijo -> ids.addAll(hijo.idsIncluidos()));
        return ids;
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof Categoria categoria && Objects.equals(id, categoria.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
