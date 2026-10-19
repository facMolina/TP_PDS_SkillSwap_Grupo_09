package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;

public class EstadoPausada extends EstadoPublicacionBase {

    @Override
    public String getNombre() {
        return "PAUSADA";
    }

    @Override
    public void reanudar(Publicacion publicacion, ContextoAccion ctx) {
        publicacion.cambiarEstado(new EstadoPublicada(), ctx, "Reanudación");
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
