package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public class PropuestaEmbebida {

    @Column(name = "propuesta_por")
    public String propuestaPor;

    @Column(name = "propuesta_inicio")
    public Instant inicio;

    @Column(name = "propuesta_fin")
    public Instant fin;

    @Column(name = "propuesta_estado")
    public String estado;
}
