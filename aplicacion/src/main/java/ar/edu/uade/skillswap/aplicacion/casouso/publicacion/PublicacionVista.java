package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;

public record PublicacionVista(Long id, UsuarioId autor, TipoPublicacion tipo, String titulo, String descripcion,
                               String categoria, Modalidad modalidad, int duracionMinutos, int cupo,
                               int cuposTomados, SkillCoins precioBase, String estado,
                               TipoPoliticaCancelacion politicaCancelacion, int franjas, int promociones) {

    public static PublicacionVista de(Publicacion publicacion) {
        return new PublicacionVista(publicacion.getId(), publicacion.getAutorId(), publicacion.getTipo(),
                publicacion.getTitulo(), publicacion.getDescripcion(),
                publicacion.getCategoria() == null ? null : publicacion.getCategoria().nombre(),
                publicacion.getModalidad(), publicacion.getDuracionMinutos(), publicacion.getCupo(),
                publicacion.getCuposTomados(), publicacion.getPrecioBase(), publicacion.getNombreEstado(),
                publicacion.getTipoPoliticaCancelacion(), publicacion.getFranjas().size(),
                publicacion.getPromociones().size());
    }
}
