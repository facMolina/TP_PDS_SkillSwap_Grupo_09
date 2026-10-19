package ar.edu.uade.skillswap.infraestructura.soporte;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class RelojPrueba extends Clock {

    private volatile Instant ahora;

    public RelojPrueba(Instant inicial) {
        this.ahora = inicial;
    }

    public void fijar(Instant instante) {
        ahora = instante;
    }

    public void avanzar(Duration duracion) {
        ahora = ahora.plus(duracion);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return this;
    }

    @Override
    public Instant instant() {
        return ahora;
    }
}
