package ar.edu.uade.skillswap.aplicacion.evento;

import ar.edu.uade.skillswap.aplicacion.puerto.UnidadDeTrabajo;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;

public class ObservadorTransaccional implements ObservadorEventos {

    private final UnidadDeTrabajo unidadDeTrabajo;
    private final ObservadorEventos observador;

    public ObservadorTransaccional(UnidadDeTrabajo unidadDeTrabajo, ObservadorEventos observador) {
        this.unidadDeTrabajo = unidadDeTrabajo;
        this.observador = observador;
    }

    @Override
    public void alRecibir(EventoContratacion evento) {
        unidadDeTrabajo.ejecutar(() -> {
            observador.alRecibir(evento);
            return null;
        });
    }
}
