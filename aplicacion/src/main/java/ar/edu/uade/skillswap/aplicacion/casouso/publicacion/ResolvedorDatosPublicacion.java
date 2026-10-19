package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.util.Set;

import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.aplicacion.puerto.CatalogoRepositorio;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.publicacion.DatosPublicacion;

public class ResolvedorDatosPublicacion {

    private final CatalogoRepositorio catalogo;

    public ResolvedorDatosPublicacion(CatalogoRepositorio catalogo) {
        this.catalogo = catalogo;
    }

    public DatosPublicacion resolver(DatosPublicacionComando comando) {
        Categoria categoria = comando.categoriaId() == null ? null : catalogo.buscarCategoria(comando.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la categoría " + comando.categoriaId()));
        Set<Habilidad> etiquetas = comando.etiquetaIds() == null ? Set.of() : catalogo.buscarHabilidades(comando.etiquetaIds());
        if (comando.etiquetaIds() != null && etiquetas.size() != comando.etiquetaIds().size()) {
            throw new RecursoNoEncontrado("Alguna de las etiquetas no existe en el catálogo");
        }
        return new DatosPublicacion(comando.tipo(), comando.titulo(), comando.descripcion(), categoria,
                comando.modalidad(), comando.duracionMinutos(), comando.cupo(), comando.precioBase(), etiquetas,
                comando.tipoPoliticaCancelacion());
    }
}
