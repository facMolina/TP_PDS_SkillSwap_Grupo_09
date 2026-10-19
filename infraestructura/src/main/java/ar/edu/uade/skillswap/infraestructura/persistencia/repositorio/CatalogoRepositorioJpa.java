package ar.edu.uade.skillswap.infraestructura.persistencia.repositorio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ar.edu.uade.skillswap.aplicacion.puerto.CatalogoRepositorio;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.catalogo.NodoCatalogo;
import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.NodoCatalogoEntidad;

public class CatalogoRepositorioJpa extends RepositorioJpa implements CatalogoRepositorio {

    public CatalogoRepositorioJpa(ContextoPersistencia contexto) {
        super(contexto);
    }

    @Override
    public Optional<Categoria> buscarCategoria(Long id) {
        List<NodoCatalogoEntidad> nodos = entityManager()
                .createQuery("select n from NodoCatalogoEntidad n order by n.id", NodoCatalogoEntidad.class)
                .getResultList();
        Map<Long, List<NodoCatalogoEntidad>> hijosPorPadre = new HashMap<>();
        nodos.stream()
                .filter(nodo -> nodo.padre != null)
                .forEach(nodo -> hijosPorPadre.computeIfAbsent(nodo.padre.id, padre -> new ArrayList<>()).add(nodo));
        return nodos.stream()
                .filter(nodo -> nodo.id.equals(id) && nodo.esCategoria())
                .findFirst()
                .map(raiz -> construir(raiz, hijosPorPadre));
    }

    @Override
    public Set<Habilidad> buscarHabilidades(Set<Long> ids) {
        if (ids.isEmpty()) {
            return Set.of();
        }
        return entityManager()
                .createQuery("select n from NodoCatalogoEntidad n where n.id in :ids and n.tipo = 'HABILIDAD'",
                        NodoCatalogoEntidad.class)
                .setParameter("ids", ids)
                .getResultList()
                .stream()
                .map(nodo -> new Habilidad(nodo.id, nodo.nombre))
                .collect(Collectors.toSet());
    }

    private Categoria construir(NodoCatalogoEntidad nodo, Map<Long, List<NodoCatalogoEntidad>> hijosPorPadre) {
        Categoria categoria = new Categoria(nodo.id, nodo.nombre);
        hijosPorPadre.getOrDefault(nodo.id, List.of()).stream()
                .sorted(Comparator.comparing(hijo -> hijo.id))
                .forEach(hijo -> categoria.agregar(nodoDominio(hijo, hijosPorPadre)));
        return categoria;
    }

    private NodoCatalogo nodoDominio(NodoCatalogoEntidad nodo, Map<Long, List<NodoCatalogoEntidad>> hijosPorPadre) {
        return nodo.esCategoria() ? construir(nodo, hijosPorPadre) : new Habilidad(nodo.id, nodo.nombre);
    }
}
