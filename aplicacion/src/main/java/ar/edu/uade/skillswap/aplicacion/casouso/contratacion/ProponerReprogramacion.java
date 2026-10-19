package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.error.FranjaNoDisponible;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class ProponerReprogramacion {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final PublicacionRepositorio publicaciones;
    private final Clock reloj;

    public ProponerReprogramacion(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                                  PublicacionRepositorio publicaciones, Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.publicaciones = publicaciones;
        this.reloj = reloj;
    }

    public ContratacionVista ejecutar(ReprogramarComando comando) {
        return transaccion.ejecutar(() -> {
            Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
            Publicacion publicacion = publicaciones.obtenerPorId(contratacion.getPublicacionId());
            if (!publicacion.tieneFranjaLibre(comando.nuevaFranja())) {
                throw new FranjaNoDisponible("La franja propuesta no está disponible");
            }
            contratacion.proponerReprogramacion(ContextoAccion.de(comando.actor(), reloj.instant()),
                    comando.nuevaFranja());
            Contratacion guardada = contrataciones.guardar(contratacion);
            return ResultadoTransaccion.de(ContratacionVista.de(guardada), guardada.extraerEventos());
        });
    }
}
