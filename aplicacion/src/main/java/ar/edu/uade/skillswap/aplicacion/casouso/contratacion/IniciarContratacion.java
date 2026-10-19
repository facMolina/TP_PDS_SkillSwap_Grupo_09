package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

public class IniciarContratacion {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final Clock reloj;

    public IniciarContratacion(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                               Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.reloj = reloj;
    }

    public ContratacionVista ejecutar(ContratacionComando comando) {
        return transaccion.ejecutar(() -> {
            Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
            contratacion.iniciar(ContextoAccion.de(comando.actor(), reloj.instant()));
            Contratacion guardada = contrataciones.guardar(contratacion);
            return ResultadoTransaccion.de(ContratacionVista.de(guardada), guardada.extraerEventos());
        });
    }
}
