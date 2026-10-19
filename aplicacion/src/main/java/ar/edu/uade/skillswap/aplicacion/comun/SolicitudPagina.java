package ar.edu.uade.skillswap.aplicacion.comun;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

public record SolicitudPagina(int numero, int tamanio) {

    public static final int TAMANIO_POR_DEFECTO = 10;
    public static final int TAMANIO_MAXIMO = 50;

    public SolicitudPagina {
        if (numero < 0) {
            throw new ValorInvalido("El número de página no puede ser negativo");
        }
        if (tamanio < 1 || tamanio > TAMANIO_MAXIMO) {
            throw new ValorInvalido("El tamaño de página debe estar entre 1 y " + TAMANIO_MAXIMO);
        }
    }

    public static SolicitudPagina primera() {
        return new SolicitudPagina(0, TAMANIO_POR_DEFECTO);
    }

    public int desplazamiento() {
        return numero * tamanio;
    }
}
