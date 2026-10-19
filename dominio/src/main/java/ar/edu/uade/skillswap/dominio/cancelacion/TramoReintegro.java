package ar.edu.uade.skillswap.dominio.cancelacion;

import java.time.Duration;

public record TramoReintegro(Duration anticipacionMinima, int porcentaje) {
}
