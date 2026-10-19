package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.error.ConflictoDeConcurrencia;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.error.CupoAgotado;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class AceptarContratacion {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final PublicacionRepositorio publicaciones;
    private final Clock reloj;

    public AceptarContratacion(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                               PublicacionRepositorio publicaciones, Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.publicaciones = publicaciones;
        this.reloj = reloj;
    }

    /**
     * Operación crítica: acepta la solicitud y reserva el cupo en una única transacción.
     * Primero se valida quién acepta y en qué estado está la solicitud; recién después se toma el cupo.
     * Dos aceptaciones simultáneas sobre el último cupo colisionan por bloqueo optimista
     * y la segunda se rechaza como cupo agotado.
     */
    public ContratacionVista ejecutar(ContratacionComando comando) {
        try {
            return transaccion.ejecutar(() -> aceptar(comando));
        } catch (ConflictoDeConcurrencia conflicto) {
            throw new CupoAgotado("El cupo fue tomado por otra aceptación");
        }
    }

    private ResultadoTransaccion<ContratacionVista> aceptar(ContratacionComando comando) {
        Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
        Publicacion publicacion = publicaciones.obtenerPorId(contratacion.getPublicacionId());
        contratacion.aceptar(ContextoAccion.de(comando.actor(), reloj.instant()));
        publicacion.reservarCupo(contratacion.getFranja());
        publicaciones.guardar(publicacion);
        Contratacion guardada = contrataciones.guardar(contratacion);
        return ResultadoTransaccion.de(ContratacionVista.de(guardada), guardada.extraerEventos());
    }
}
