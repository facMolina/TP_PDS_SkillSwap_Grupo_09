package ar.edu.uade.skillswap.dominio.contratacion;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import ar.edu.uade.skillswap.dominio.cancelacion.PoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;

public class EstadoAceptada extends EstadoContratacionBase {

    @Override
    public String getNombre() {
        return "ACEPTADA";
    }

    @Override
    public void confirmarPago(Contratacion contratacion, ContextoAccion ctx) {
        contratacion.exigirCliente(ctx);
        contratacion.retenerPago();
        contratacion.cambiarEstado(new EstadoConfirmada(), ctx, "Pago confirmado");
        contratacion.registrarEvento(TipoEvento.PAGO_CONFIRMADO, ctx, contratacion.ambasPartes());
    }

    @Override
    public ResultadoCancelacion cancelar(Contratacion contratacion, ContextoAccion ctx, PoliticaCancelacion politica,
                                         String motivo) {
        contratacion.exigirParticipante(ctx);
        contratacion.cambiarEstado(new EstadoCancelada(), ctx, motivo);
        contratacion.registrarEvento(TipoEvento.CONTRATACION_CANCELADA, ctx,
                List.of(contratacion.contraparteDe(ctx.actor())));
        return ResultadoCancelacion.sinMovimientos();
    }

    @Override
    public boolean vencerSinPago(Contratacion contratacion, Instant ahora, Duration plazo) {
        if (ahora.isBefore(contratacion.getFechaAceptacion().plus(plazo))) {
            return false;
        }
        ContextoAccion sistema = ContextoAccion.delSistema(ahora);
        contratacion.cambiarEstado(new EstadoCancelada(), sistema, "Vencimiento del plazo de pago");
        contratacion.registrarEvento(TipoEvento.CONTRATACION_CANCELADA, sistema, contratacion.ambasPartes());
        return true;
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
