package ar.edu.uade.skillswap.aplicacion.puerto;

import java.util.Optional;
import java.util.Set;

import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;

public interface CatalogoRepositorio {

    Optional<Categoria> buscarCategoria(Long id);

    Set<Habilidad> buscarHabilidades(Set<Long> ids);
}
