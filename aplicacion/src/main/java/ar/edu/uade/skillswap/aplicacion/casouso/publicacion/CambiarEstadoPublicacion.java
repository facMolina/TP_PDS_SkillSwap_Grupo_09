package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.error.OperacionNoPermitida;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class CambiarEstadoPublicacion {

    private final EjecutorTransaccional transaccion;
    private final PublicacionRepositorio publicaciones;
    private final ContratacionRepositorio contrataciones;
    private final RechazadorDeSolicitudes rechazador;
    private final Clock reloj;

    public CambiarEstadoPublicacion(EjecutorTransaccional transaccion, PublicacionRepositorio publicaciones,
                                    ContratacionRepositorio contrataciones, RechazadorDeSolicitudes rechazador,
                                    Clock reloj) {
        this.transaccion = transaccion;
        this.publicaciones = publicaciones;
        this.contrataciones = contrataciones;
        this.rechazador = rechazador;
        this.reloj = reloj;
    }

    public PublicacionVista ejecutar(CambiarEstadoComando comando) {
        return transaccion.ejecutar(() -> {
            Instant ahora = reloj.instant();
            Publicacion publicacion = publicaciones.obtenerPorId(comando.publicacionId());
            if (comando.accion().retiraDeCirculacion()
                    && contrataciones.existenComprometidasDePublicacion(comando.publicacionId())) {
                throw new OperacionNoPermitida("La publicación tiene contrataciones comprometidas");
            }
            comando.accion().aplicar(publicacion, ContextoAccion.de(comando.actor(), ahora), comando.motivo());
            List<EventoContratacion> eventos = comando.accion().retiraDeCirculacion()
                    ? rechazador.rechazar(comando.publicacionId(), ahora, "La publicación fue retirada")
                    : List.of();
            return ResultadoTransaccion.de(PublicacionVista.de(publicaciones.guardar(publicacion)), eventos);
        });
    }
}
