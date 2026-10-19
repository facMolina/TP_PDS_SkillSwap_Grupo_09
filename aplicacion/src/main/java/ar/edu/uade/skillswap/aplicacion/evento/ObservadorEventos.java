package ar.edu.uade.skillswap.aplicacion.evento;

import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;

public interface ObservadorEventos {

    void alRecibir(EventoContratacion evento);
}
