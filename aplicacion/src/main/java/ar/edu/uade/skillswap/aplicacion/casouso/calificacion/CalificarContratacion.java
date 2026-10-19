package ar.edu.uade.skillswap.aplicacion.casouso.calificacion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.CalificacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.dominio.calificacion.Calificacion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.error.CalificacionNoPermitida;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class CalificarContratacion {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final CalificacionRepositorio calificaciones;
    private final PerfilRepositorio perfiles;
    private final Clock reloj;

    public CalificarContratacion(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                                 CalificacionRepositorio calificaciones, PerfilRepositorio perfiles, Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.calificaciones = calificaciones;
        this.perfiles = perfiles;
        this.reloj = reloj;
    }

    public CalificacionVista ejecutar(CalificarComando comando) {
        return transaccion.ejecutar(() -> {
            Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
            Calificacion calificacion = Calificacion.registrar(contratacion, comando.actor(), comando.puntaje(),
                    comando.comentario(), reloj.instant());
            if (calificaciones.existe(contratacion.getId(), comando.actor())) {
                throw new CalificacionNoPermitida("Ya calificaste esta contratación");
            }
            Perfil receptor = perfiles.obtenerPorUsuario(calificacion.getReceptorId());
            receptor.recibirCalificacion(calificacion.getPuntaje());
            perfiles.guardar(receptor);
            return ResultadoTransaccion.sinEventos(CalificacionVista.de(calificaciones.guardar(calificacion)));
        });
    }
}
