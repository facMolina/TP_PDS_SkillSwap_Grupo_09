package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public class TransicionEmbebida {

    @Column(nullable = false)
    public Instant fecha;

    @Column(name = "actor_id", nullable = false)
    public String actorId;

    @Column(nullable = false)
    public String origen;

    @Column(nullable = false)
    public String destino;

    public String motivo;
}
