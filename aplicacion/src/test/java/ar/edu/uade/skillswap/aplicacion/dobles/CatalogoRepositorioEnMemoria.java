package ar.edu.uade.skillswap.aplicacion.dobles;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ar.edu.uade.skillswap.aplicacion.puerto.CatalogoRepositorio;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;

public class CatalogoRepositorioEnMemoria implements CatalogoRepositorio {

    private final Map<Long, Categoria> categorias = new HashMap<>();
    private final Map<Long, Habilidad> habilidades = new HashMap<>();

    public void agregar(Categoria categoria) {
        categorias.put(categoria.id(), categoria);
    }

    public void agregar(Habilidad habilidad) {
        habilidades.put(habilidad.id(), habilidad);
    }

    @Override
    public Optional<Categoria> buscarCategoria(Long id) {
        return Optional.ofNullable(categorias.get(id));
    }

    @Override
    public Set<Habilidad> buscarHabilidades(Set<Long> ids) {
        return ids.stream().map(habilidades::get).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
    }
}
