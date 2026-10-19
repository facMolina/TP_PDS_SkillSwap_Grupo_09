package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.contratacion.PropuestaReprogramacion;
import ar.edu.uade.skillswap.dominio.error.FranjaNoDisponible;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class ResponderReprogramacion {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final PublicacionRepositorio publicaciones;
    private final Clock reloj;

    public ResponderReprogramacion(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                                   PublicacionRepositorio publicaciones, Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.publicaciones = publicaciones;
        this.reloj = reloj;
    }

    public ContratacionVista ejecutar(ResponderReprogramacionComando comando) {
        return transaccion.ejecutar(() -> {
            Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
            Publicacion publicacion = publicaciones.obtenerPorId(contratacion.getPublicacionId());
            FranjaHoraria anterior = contratacion.getFranja();
            PropuestaReprogramacion propuesta = contratacion.getPropuesta();
            boolean mueveLaReserva = comando.acepta() && propuesta != null && propuesta.estaPendiente();
            if (mueveLaReserva && !publicacion.tieneFranjaLibre(propuesta.getNuevaFranja())) {
                throw new FranjaNoDisponible("La franja propuesta ya no está disponible");
            }
            contratacion.responderReprogramacion(ContextoAccion.de(comando.actor(), reloj.instant()),
                    comando.acepta());
            if (mueveLaReserva) {
                publicacion.cambiarFranja(anterior, contratacion.getFranja());
                publicaciones.guardar(publicacion);
            }
            Contratacion guardada = contrataciones.guardar(contratacion);
            return ResultadoTransaccion.de(ContratacionVista.de(guardada), guardada.extraerEventos());
        });
    }
}
