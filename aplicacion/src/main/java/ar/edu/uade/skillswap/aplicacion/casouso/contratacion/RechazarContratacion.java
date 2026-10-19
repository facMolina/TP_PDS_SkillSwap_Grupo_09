package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

public class RechazarContratacion {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final Clock reloj;

    public RechazarContratacion(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                                Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.reloj = reloj;
    }

    public ContratacionVista ejecutar(RechazarComando comando) {
        return transaccion.ejecutar(() -> {
            Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
            contratacion.rechazar(ContextoAccion.de(comando.actor(), reloj.instant()), comando.motivo());
            Contratacion guardada = contrataciones.guardar(contratacion);
            return ResultadoTransaccion.de(ContratacionVista.de(guardada), guardada.extraerEventos());
        });
    }
}
