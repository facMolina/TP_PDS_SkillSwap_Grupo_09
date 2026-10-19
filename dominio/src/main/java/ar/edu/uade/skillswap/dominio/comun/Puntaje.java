package ar.edu.uade.skillswap.dominio.comun;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

public record Puntaje(int valor) {

    public static final int MINIMO = 1;
    public static final int MAXIMO = 5;

    public Puntaje {
        if (valor < MINIMO || valor > MAXIMO) {
            throw new ValorInvalido("El puntaje debe estar entre " + MINIMO + " y " + MAXIMO);
        }
    }
}
