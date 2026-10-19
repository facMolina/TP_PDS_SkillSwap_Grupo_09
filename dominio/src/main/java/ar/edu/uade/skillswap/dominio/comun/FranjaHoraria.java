package ar.edu.uade.skillswap.dominio.comun;

import java.time.Duration;
import java.time.Instant;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

public record FranjaHoraria(Instant inicio, Instant fin) {

    public FranjaHoraria {
        if (inicio == null || fin == null || !inicio.isBefore(fin)) {
            throw new ValorInvalido("La franja debe tener un inicio anterior a su fin");
        }
    }

    public boolean seSolapaCon(FranjaHoraria otra) {
        return inicio.isBefore(otra.fin) && otra.inicio.isBefore(fin);
    }

    public boolean contiene(FranjaHoraria otra) {
        return !otra.inicio.isBefore(inicio) && !otra.fin.isAfter(fin);
    }

    public Duration duracion() {
        return Duration.between(inicio, fin);
    }
}
