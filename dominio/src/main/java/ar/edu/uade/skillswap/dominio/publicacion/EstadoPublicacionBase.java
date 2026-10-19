package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;

public abstract class EstadoPublicacionBase implements EstadoPublicacion {

    @Override
    public boolean permiteEdicion() {
        return false;
    }

    @Override
    public boolean esFinal() {
        return false;
    }

    @Override
    public boolean admiteSolicitudes() {
        return false;
    }

    @Override
    public boolean esVisiblePara(Publicacion publicacion, UsuarioId usuario) {
        return publicacion.getAutorId().equals(usuario);
    }

    @Override
    public void publicar(Publicacion publicacion, ContextoAccion ctx) {
        throw rechazo("publicar");
    }

    @Override
    public void pausar(Publicacion publicacion, ContextoAccion ctx) {
        throw rechazo("pausar");
    }

    @Override
    public void reanudar(Publicacion publicacion, ContextoAccion ctx) {
        throw rechazo("reanudar");
    }

    @Override
    public void cerrar(Publicacion publicacion, ContextoAccion ctx) {
        throw rechazo("cerrar");
    }

    @Override
    public void cancelar(Publicacion publicacion, ContextoAccion ctx, String motivo) {
        throw rechazo("cancelar");
    }

    private TransicionInvalida rechazo(String accion) {
        return new TransicionInvalida("No se puede " + accion + " una publicación en estado " + getNombre());
    }
}
