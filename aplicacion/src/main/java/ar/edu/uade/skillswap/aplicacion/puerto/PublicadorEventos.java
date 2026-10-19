package ar.edu.uade.skillswap.aplicacion.puerto;

import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;

public interface PublicadorEventos {

    void publicar(EventoContratacion evento);
}
