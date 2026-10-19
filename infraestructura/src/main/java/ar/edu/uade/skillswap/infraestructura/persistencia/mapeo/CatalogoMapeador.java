package ar.edu.uade.skillswap.infraestructura.persistencia.mapeo;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.NodoCatalogoEntidad;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;

final class CatalogoMapeador {

    private CatalogoMapeador() {
    }

    static Habilidad aHabilidad(NodoCatalogoEntidad referencia) {
        NodoCatalogoEntidad nodo = desreferenciar(referencia);
        return new Habilidad(nodo.id, nodo.nombre);
    }

    static Set<Habilidad> aHabilidades(Set<NodoCatalogoEntidad> nodos) {
        return nodos.stream().map(CatalogoMapeador::aHabilidad).collect(Collectors.toSet());
    }

    static Categoria aCategoria(NodoCatalogoEntidad referencia) {
        NodoCatalogoEntidad nodo = desreferenciar(referencia);
        return new Categoria(nodo.id, nodo.nombre);
    }

    static void sincronizar(Set<NodoCatalogoEntidad> destino, Set<Habilidad> origen, EntityManager entityManager) {
        Set<Long> ids = origen.stream().map(Habilidad::id).collect(Collectors.toSet());
        destino.removeIf(nodo -> !ids.contains(desreferenciar(nodo).id));
        Set<Long> existentes = new HashSet<>();
        destino.forEach(nodo -> existentes.add(desreferenciar(nodo).id));
        ids.stream()
                .filter(id -> !existentes.contains(id))
                .forEach(id -> destino.add(entityManager.getReference(NodoCatalogoEntidad.class, id)));
    }

    private static NodoCatalogoEntidad desreferenciar(NodoCatalogoEntidad referencia) {
        return Hibernate.unproxy(referencia, NodoCatalogoEntidad.class);
    }
}
