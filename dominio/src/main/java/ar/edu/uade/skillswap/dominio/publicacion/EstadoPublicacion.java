package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public interface EstadoPublicacion {

    String getNombre();

    boolean permiteEdicion();

    boolean esFinal();

    boolean admiteSolicitudes();

    boolean esVisiblePara(Publicacion publicacion, UsuarioId usuario);

    void publicar(Publicacion publicacion, ContextoAccion ctx);

    void pausar(Publicacion publicacion, ContextoAccion ctx);

    void reanudar(Publicacion publicacion, ContextoAccion ctx);

    void cerrar(Publicacion publicacion, ContextoAccion ctx);

    void cancelar(Publicacion publicacion, ContextoAccion ctx, String motivo);
}
