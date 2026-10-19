package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;

public class EstadoBorrador extends EstadoPublicacionBase {

    @Override
    public String getNombre() {
        return "BORRADOR";
    }

    @Override
    public boolean permiteEdicion() {
        return true;
    }

    @Override
    public void publicar(Publicacion publicacion, ContextoAccion ctx) {
        publicacion.exigirCompleta();
        publicacion.cambiarEstado(new EstadoPublicada(), ctx, "Publicación");
    }
}
