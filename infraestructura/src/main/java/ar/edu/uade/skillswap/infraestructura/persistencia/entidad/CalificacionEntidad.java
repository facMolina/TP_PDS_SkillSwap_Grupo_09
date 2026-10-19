package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "calificacion")
public class CalificacionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "contratacion_id", nullable = false, updatable = false)
    public Long contratacionId;

    @Column(name = "autor_id", nullable = false, updatable = false)
    public String autorId;

    @Column(name = "receptor_id", nullable = false, updatable = false)
    public String receptorId;

    @Column(nullable = false, updatable = false)
    public int puntaje;

    @Column(updatable = false)
    public String comentario;

    @Column(nullable = false, updatable = false)
    public Instant fecha;
}
