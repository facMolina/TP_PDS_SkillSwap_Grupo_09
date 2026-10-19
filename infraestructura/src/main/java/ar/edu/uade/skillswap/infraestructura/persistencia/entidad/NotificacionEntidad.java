package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notificacion")
public class NotificacionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "destinatario_id", nullable = false, updatable = false)
    public String destinatarioId;

    @Column(nullable = false, updatable = false)
    public String tipo;

    @Column(nullable = false, updatable = false)
    public String mensaje;

    @Column(nullable = false, updatable = false)
    public Instant fecha;

    @Column(nullable = false)
    public boolean leida;
}
