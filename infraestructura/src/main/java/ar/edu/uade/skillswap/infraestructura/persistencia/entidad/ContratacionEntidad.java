package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contratacion")
public class ContratacionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "publicacion_id", nullable = false, updatable = false)
    public Long publicacionId;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    public String clienteId;

    @Column(name = "proveedor_id", nullable = false, updatable = false)
    public String proveedorId;

    @Column(updatable = false)
    public String mensaje;

    @Column(updatable = false)
    public String objetivo;

    @Column(name = "franja_inicio", nullable = false)
    public Instant franjaInicio;

    @Column(name = "franja_fin", nullable = false)
    public Instant franjaFin;

    @Column(name = "precio_acordado", nullable = false, updatable = false)
    public long precioAcordado;

    @Column(name = "monto_retenido", nullable = false)
    public long montoRetenido;

    @Column(name = "politica_cancelacion", nullable = false, updatable = false)
    public String politicaCancelacion;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    public Instant fechaSolicitud;

    @Column(name = "fecha_aceptacion")
    public Instant fechaAceptacion;

    @Column(name = "fecha_completada")
    public Instant fechaCompletada;

    @Column(nullable = false)
    public String estado;

    @Embedded
    public PropuestaEmbebida propuesta;

    @Version
    public long version;

    @ElementCollection
    @CollectionTable(name = "transicion_contratacion", joinColumns = @JoinColumn(name = "contratacion_id"))
    @OrderColumn(name = "orden")
    public List<TransicionEmbebida> historial = new ArrayList<>();
}
