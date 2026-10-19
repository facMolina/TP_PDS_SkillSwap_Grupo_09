package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public class FranjaDisponibleEmbebida {

    @Column(nullable = false)
    public Instant inicio;

    @Column(nullable = false)
    public Instant fin;

    @Column(nullable = false)
    public boolean ocupada;
}
