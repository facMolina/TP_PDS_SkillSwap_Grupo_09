package ar.edu.uade.skillswap.dominio.contratacion;

import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;

public class EstadoEnCurso extends EstadoContratacionBase {

    @Override
    public String getNombre() {
        return "EN_CURSO";
    }

    @Override
    public SkillCoins completar(Contratacion contratacion, ContextoAccion ctx) {
        contratacion.exigirProveedor(ctx);
        SkillCoins liberado = contratacion.liberarRetencion();
        contratacion.registrarCompletada(ctx.ahora());
        contratacion.cambiarEstado(new EstadoCompletada(), ctx, "Sesión completada");
        contratacion.registrarEvento(TipoEvento.SESION_COMPLETADA, ctx, contratacion.ambasPartes());
        return liberado;
    }

    @Override
    public boolean estaComprometida() {
        return true;
    }
}
