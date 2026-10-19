package ar.edu.uade.skillswap.dominio.contratacion;

import java.time.Duration;
import java.time.Instant;

import ar.edu.uade.skillswap.dominio.cancelacion.PoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public interface EstadoContratacion {

    String getNombre();

    void aceptar(Contratacion contratacion, ContextoAccion ctx);

    void rechazar(Contratacion contratacion, ContextoAccion ctx, String motivo);

    void confirmarPago(Contratacion contratacion, ContextoAccion ctx);

    void iniciar(Contratacion contratacion, ContextoAccion ctx);

    SkillCoins completar(Contratacion contratacion, ContextoAccion ctx);

    ResultadoCancelacion cancelar(Contratacion contratacion, ContextoAccion ctx, PoliticaCancelacion politica,
                                  String motivo);

    boolean vencerSinPago(Contratacion contratacion, Instant ahora, Duration plazo);

    void proponerReprogramacion(Contratacion contratacion, ContextoAccion ctx, FranjaHoraria nuevaFranja);

    void responderReprogramacion(Contratacion contratacion, ContextoAccion ctx, boolean acepta);

    boolean estaComprometida();

    boolean estaCompletada();
}
