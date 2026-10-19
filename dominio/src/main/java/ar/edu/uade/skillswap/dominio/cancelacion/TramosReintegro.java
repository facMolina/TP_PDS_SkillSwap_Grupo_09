package ar.edu.uade.skillswap.dominio.cancelacion;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

public class TramosReintegro {

    private final List<TramoReintegro> tramos;

    public TramosReintegro(List<TramoReintegro> tramos) {
        if (tramos == null || tramos.isEmpty()) {
            throw new ValorInvalido("Debe definirse al menos un tramo de reintegro");
        }
        this.tramos = tramos.stream()
                .sorted(Comparator.comparing(TramoReintegro::anticipacionMinima).reversed())
                .toList();
    }

    public int porcentajePara(Duration anticipacion) {
        return tramos.stream()
                .filter(tramo -> anticipacion.compareTo(tramo.anticipacionMinima()) >= 0)
                .map(TramoReintegro::porcentaje)
                .findFirst()
                .orElse(0);
    }
}
