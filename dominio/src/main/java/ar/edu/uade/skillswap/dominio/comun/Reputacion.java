package ar.edu.uade.skillswap.dominio.comun;

public record Reputacion(long sumaPuntajes, int cantidad) {

    public static final Reputacion SIN_CALIFICACIONES = new Reputacion(0, 0);

    public Reputacion incorporar(Puntaje puntaje) {
        return new Reputacion(sumaPuntajes + puntaje.valor(), cantidad + 1);
    }

    public double promedio() {
        if (cantidad == 0) {
            return 0;
        }
        return Math.round((double) sumaPuntajes / cantidad * 100) / 100.0;
    }
}
