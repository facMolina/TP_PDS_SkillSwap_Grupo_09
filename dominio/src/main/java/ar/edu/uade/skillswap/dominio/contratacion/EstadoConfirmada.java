package ar.edu.uade.skillswap.dominio.contratacion;

import java.util.List;

import ar.edu.uade.skillswap.dominio.cancelacion.PoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;

public class EstadoConfirmada extends EstadoContratacionBase {

    @Override
    public String getNombre() {
        return "CONFIRMADA";
    }

    @Override
    public void iniciar(Contratacion contratacion, ContextoAccion ctx) {
        contratacion.exigirProveedor(ctx);
        contratacion.cambiarEstado(new EstadoEnCurso(), ctx, "Inicio de la sesión");
    }

    @Override
    public ResultadoCancelacion cancelar(Contratacion contratacion, ContextoAccion ctx, PoliticaCancelacion politica,
                                         String motivo) {
        contratacion.exigirParticipante(ctx);
        SkillCoins retenido = contratacion.liberarRetencion();
        SkillCoins reintegro = contratacion.esProveedor(ctx.actor())
                ? retenido
                : politica.calcularReintegro(retenido, contratacion.anticipacionDesde(ctx.ahora()));
        ResultadoCancelacion resultado = new ResultadoCancelacion(reintegro, retenido.restar(reintegro));
        contratacion.cambiarEstado(new EstadoCancelada(), ctx, motivo);
        contratacion.registrarEvento(TipoEvento.CONTRATACION_CANCELADA, ctx,
                List.of(contratacion.contraparteDe(ctx.actor())));
        return resultado;
    }

    @Override
    public void proponerReprogramacion(Contratacion contratacion, ContextoAccion ctx, FranjaHoraria nuevaFranja) {
        contratacion.abrirPropuesta(ctx, nuevaFranja);
    }

    @Override
    public void responderReprogramacion(Contratacion contratacion, ContextoAccion ctx, boolean acepta) {
        contratacion.resolverPropuesta(ctx, acepta);
    }

    @Override
    public boolean estaComprometida() {
        return true;
    }
}
