package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public class PeriodoEmbebido {

    @Column(nullable = false)
    public Instant inicio;

    @Column(nullable = false)
    public Instant fin;

    protected PeriodoEmbebido() {
    }

    public PeriodoEmbebido(Instant inicio, Instant fin) {
        this.inicio = inicio;
        this.fin = fin;
    }
}
