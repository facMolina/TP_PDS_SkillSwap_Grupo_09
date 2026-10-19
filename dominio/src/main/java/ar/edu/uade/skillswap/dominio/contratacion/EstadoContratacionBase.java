package ar.edu.uade.skillswap.dominio.contratacion;

import java.time.Duration;
import java.time.Instant;

import ar.edu.uade.skillswap.dominio.cancelacion.PoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;

public abstract class EstadoContratacionBase implements EstadoContratacion {

    @Override
    public void aceptar(Contratacion contratacion, ContextoAccion ctx) {
        throw rechazo("aceptar");
    }

    @Override
    public void rechazar(Contratacion contratacion, ContextoAccion ctx, String motivo) {
        throw rechazo("rechazar");
    }

    @Override
    public void confirmarPago(Contratacion contratacion, ContextoAccion ctx) {
        throw rechazo("pagar");
    }

    @Override
    public void iniciar(Contratacion contratacion, ContextoAccion ctx) {
        throw rechazo("iniciar");
    }

    @Override
    public SkillCoins completar(Contratacion contratacion, ContextoAccion ctx) {
        throw rechazo("completar");
    }

    @Override
    public ResultadoCancelacion cancelar(Contratacion contratacion, ContextoAccion ctx, PoliticaCancelacion politica,
                                         String motivo) {
        throw rechazo("cancelar");
    }

    @Override
    public boolean vencerSinPago(Contratacion contratacion, Instant ahora, Duration plazo) {
        return false;
    }

    @Override
    public void proponerReprogramacion(Contratacion contratacion, ContextoAccion ctx, FranjaHoraria nuevaFranja) {
        throw rechazo("reprogramar");
    }

    @Override
    public void responderReprogramacion(Contratacion contratacion, ContextoAccion ctx, boolean acepta) {
        throw rechazo("responder una reprogramación de");
    }

    @Override
    public boolean estaComprometida() {
        return false;
    }

    @Override
    public boolean estaCompletada() {
        return false;
    }

    private TransicionInvalida rechazo(String accion) {
        return new TransicionInvalida("No se puede " + accion + " una contratación en estado " + getNombre());
    }
}
