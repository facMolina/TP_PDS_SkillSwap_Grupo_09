package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public class EstadoPublicada extends EstadoPublicacionBase {

    @Override
    public String getNombre() {
        return "PUBLICADA";
    }

    @Override
    public boolean admiteSolicitudes() {
        return true;
    }

    @Override
    public boolean esVisiblePara(Publicacion publicacion, UsuarioId usuario) {
        return true;
    }

    @Override
    public void pausar(Publicacion publicacion, ContextoAccion ctx) {
        publicacion.cambiarEstado(new EstadoPausada(), ctx, "Pausa");
    }

    @Override
    public void cerrar(Publicacion publicacion, ContextoAccion ctx) {
        publicacion.cambiarEstado(new EstadoCerrada(), ctx, "Cierre");
    }

    @Override
    public void cancelar(Publicacion publicacion, ContextoAccion ctx, String motivo) {
        publicacion.cambiarEstado(new EstadoCancelada(), ctx, motivo);
    }
}
