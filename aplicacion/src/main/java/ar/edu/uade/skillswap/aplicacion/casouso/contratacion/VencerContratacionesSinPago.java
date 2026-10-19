package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ar.edu.uade.skillswap.aplicacion.comun.ConfiguracionAplicacion;
import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class VencerContratacionesSinPago {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final PublicacionRepositorio publicaciones;
    private final ConfiguracionAplicacion configuracion;
    private final Clock reloj;

    public VencerContratacionesSinPago(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                                       PublicacionRepositorio publicaciones, ConfiguracionAplicacion configuracion,
                                       Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.publicaciones = publicaciones;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    public int ejecutar() {
        return transaccion.ejecutar(() -> {
            Instant ahora = reloj.instant();
            Duration plazo = configuracion.plazoPago();
            List<EventoContratacion> eventos = new ArrayList<>();
            int vencidas = 0;
            for (Contratacion contratacion : contrataciones.buscarAceptadasSinPagoDesde(ahora.minus(plazo))) {
                FranjaHoraria franja = contratacion.getFranja();
                if (contratacion.vencerSinPago(ahora, plazo)) {
                    Publicacion publicacion = publicaciones.obtenerPorId(contratacion.getPublicacionId());
                    publicacion.liberarCupo(franja);
                    publicaciones.guardar(publicacion);
                    contrataciones.guardar(contratacion);
                    eventos.addAll(contratacion.extraerEventos());
                    vencidas++;
                }
            }
            return ResultadoTransaccion.de(vencidas, eventos);
        });
    }
}
