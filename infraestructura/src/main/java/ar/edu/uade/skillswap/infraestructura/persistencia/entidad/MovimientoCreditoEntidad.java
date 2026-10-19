package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "movimiento_credito")
public class MovimientoCreditoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "billetera_id", nullable = false, updatable = false)
    public Long billeteraId;

    @Column(nullable = false, updatable = false)
    public String tipo;

    @Column(nullable = false, updatable = false)
    public long monto;

    @Column(name = "saldo_resultante", nullable = false, updatable = false)
    public long saldoResultante;

    @Column(updatable = false)
    public String referencia;

    @Column(nullable = false, updatable = false)
    public Instant fecha;
}
