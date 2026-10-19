package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.error.FranjaNoDisponible;

public class FranjaDisponible {

    private final FranjaHoraria franja;
    private boolean ocupada;

    public FranjaDisponible(FranjaHoraria franja) {
        this.franja = franja;
    }

    public FranjaDisponible(FranjaHoraria franja, boolean ocupada) {
        this.franja = franja;
        this.ocupada = ocupada;
    }

    public void reservar() {
        if (ocupada) {
            throw new FranjaNoDisponible("La franja ya está reservada");
        }
        ocupada = true;
    }

    public void liberar() {
        ocupada = false;
    }

    public boolean estaOcupada() {
        return ocupada;
    }

    public boolean coincideCon(FranjaHoraria otra) {
        return franja.equals(otra);
    }

    public FranjaHoraria getFranja() {
        return franja;
    }
}
