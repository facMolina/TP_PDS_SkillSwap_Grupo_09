package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public class PromocionEmbebida {

    public static final String PORCENTAJE = "PORCENTAJE";
    public static final String MONTO_FIJO = "MONTO_FIJO";

    @Column(nullable = false)
    public String tipo;

    public Integer porcentaje;

    public Long monto;

    @Column(name = "vigente_desde", nullable = false)
    public Instant vigenteDesde;

    @Column(name = "vigente_hasta", nullable = false)
    public Instant vigenteHasta;
}
