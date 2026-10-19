package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

public class SolicitarContratacion {

    private final EjecutorTransaccional transaccion;
    private final PublicacionRepositorio publicaciones;
    private final PerfilRepositorio perfiles;
    private final ContratacionRepositorio contrataciones;
    private final Clock reloj;

    public SolicitarContratacion(EjecutorTransaccional transaccion, PublicacionRepositorio publicaciones,
                                 PerfilRepositorio perfiles, ContratacionRepositorio contrataciones, Clock reloj) {
        this.transaccion = transaccion;
        this.publicaciones = publicaciones;
        this.perfiles = perfiles;
        this.contrataciones = contrataciones;
        this.reloj = reloj;
    }

    public ContratacionVista ejecutar(SolicitarComando comando) {
        return transaccion.ejecutar(() -> {
            Contratacion contratacion = Contratacion.solicitar(
                    publicaciones.obtenerPorId(comando.publicacionId()),
                    perfiles.obtenerPorUsuario(comando.actor()),
                    comando.mensaje(), comando.objetivo(), comando.franja(), reloj.instant());
            Contratacion guardada = contrataciones.guardar(contratacion);
            return ResultadoTransaccion.de(ContratacionVista.de(guardada), guardada.extraerEventos());
        });
    }
}
