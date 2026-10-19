package ar.edu.uade.skillswap.aplicacion.comun;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> elementos, int numero, int tamanio, long total) {

    public <R> Pagina<R> transformar(Function<T, R> funcion) {
        return new Pagina<>(elementos.stream().map(funcion).toList(), numero, tamanio, total);
    }

    public static <T> Pagina<T> de(List<T> todos, SolicitudPagina solicitud) {
        int desde = Math.min(solicitud.desplazamiento(), todos.size());
        int hasta = Math.min(desde + solicitud.tamanio(), todos.size());
        return new Pagina<>(todos.subList(desde, hasta), solicitud.numero(), solicitud.tamanio(), todos.size());
    }

    public long totalPaginas() {
        return (total + tamanio - 1) / tamanio;
    }
}
