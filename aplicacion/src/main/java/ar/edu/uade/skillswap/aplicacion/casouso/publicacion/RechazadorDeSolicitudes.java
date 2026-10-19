package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;

public class RechazadorDeSolicitudes {

    private final ContratacionRepositorio contrataciones;

    public RechazadorDeSolicitudes(ContratacionRepositorio contrataciones) {
        this.contrataciones = contrataciones;
    }

    public List<EventoContratacion> rechazar(Long publicacionId, Instant ahora, String motivo) {
        ContextoAccion sistema = ContextoAccion.delSistema(ahora);
        List<EventoContratacion> eventos = new ArrayList<>();
        for (Contratacion pendiente : contrataciones.buscarPendientesDePublicacion(publicacionId)) {
            pendiente.rechazar(sistema, motivo);
            contrataciones.guardar(pendiente);
            eventos.addAll(pendiente.extraerEventos());
        }
        return eventos;
    }
}
