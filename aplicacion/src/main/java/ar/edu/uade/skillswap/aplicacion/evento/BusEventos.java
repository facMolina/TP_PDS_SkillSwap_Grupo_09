package ar.edu.uade.skillswap.aplicacion.evento;

import java.util.ArrayList;
import java.util.List;

import ar.edu.uade.skillswap.aplicacion.puerto.PublicadorEventos;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;

public class BusEventos implements PublicadorEventos {

    private final List<ObservadorEventos> observadores = new ArrayList<>();

    public void suscribir(ObservadorEventos observador) {
        observadores.add(observador);
    }

    @Override
    public void publicar(EventoContratacion evento) {
        observadores.forEach(observador -> observador.alRecibir(evento));
    }
}
