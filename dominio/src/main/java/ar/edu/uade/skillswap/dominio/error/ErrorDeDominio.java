package ar.edu.uade.skillswap.dominio.error;

public class ErrorDeDominio extends RuntimeException {

    public ErrorDeDominio(String mensaje) {
        super(mensaje);
    }
}
