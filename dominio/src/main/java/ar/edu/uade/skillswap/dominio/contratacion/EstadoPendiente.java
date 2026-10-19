package ar.edu.uade.skillswap.dominio.contratacion;

import java.util.List;

import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;

public class EstadoPendiente extends EstadoContratacionBase {

    @Override
    public String getNombre() {
        return "PENDIENTE";
    }

    @Override
    public void aceptar(Contratacion contratacion, ContextoAccion ctx) {
        contratacion.exigirProveedor(ctx);
        contratacion.registrarAceptacion(ctx.ahora());
        contratacion.cambiarEstado(new EstadoAceptada(), ctx, "Solicitud aceptada");
        contratacion.registrarEvento(TipoEvento.SOLICITUD_ACEPTADA, ctx, List.of(contratacion.getClienteId()));
    }

    @Override
    public void rechazar(Contratacion contratacion, ContextoAccion ctx, String motivo) {
        contratacion.exigirProveedorOSistema(ctx);
        contratacion.cambiarEstado(new EstadoRechazada(), ctx, motivo);
        contratacion.registrarEvento(TipoEvento.SOLICITUD_RECHAZADA, ctx, List.of(contratacion.getClienteId()));
    }
}
