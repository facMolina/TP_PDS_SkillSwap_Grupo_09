package ar.edu.uade.skillswap.aplicacion.error;

public class ErrorDeAplicacion extends RuntimeException {

    public ErrorDeAplicacion(String mensaje) {
        super(mensaje);
    }
}
