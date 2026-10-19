package ar.edu.uade.skillswap.aplicacion.comun;

import java.util.List;

import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;

public record ResultadoTransaccion<T>(T valor, List<EventoContratacion> eventos) {

    public static <T> ResultadoTransaccion<T> de(T valor, List<EventoContratacion> eventos) {
        return new ResultadoTransaccion<>(valor, eventos);
    }

    public static <T> ResultadoTransaccion<T> sinEventos(T valor) {
        return new ResultadoTransaccion<>(valor, List.of());
    }
}
