package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "publicacion")
public class PublicacionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "autor_id", nullable = false, updatable = false)
    public String autorId;

    @Column(nullable = false, updatable = false)
    public String tipo;

    public String titulo;

    public String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    public NodoCatalogoEntidad categoria;

    public String modalidad;

    @Column(name = "duracion_minutos", nullable = false)
    public int duracionMinutos;

    @Column(nullable = false)
    public int cupo;

    @Column(name = "cupos_tomados", nullable = false)
    public int cuposTomados;

    @Column(name = "precio_base")
    public Long precioBase;

    @Column(name = "politica_cancelacion", nullable = false)
    public String politicaCancelacion;

    @Column(nullable = false)
    public String estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    public Instant fechaCreacion;

    @Version
    public long version;

    @ManyToMany
    @JoinTable(name = "publicacion_etiqueta", joinColumns = @JoinColumn(name = "publicacion_id"),
            inverseJoinColumns = @JoinColumn(name = "habilidad_id"))
    public Set<NodoCatalogoEntidad> etiquetas = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "franja_disponible", joinColumns = @JoinColumn(name = "publicacion_id"))
    @OrderColumn(name = "orden")
    public List<FranjaDisponibleEmbebida> franjas = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "promocion", joinColumns = @JoinColumn(name = "publicacion_id"))
    @OrderColumn(name = "orden")
    public List<PromocionEmbebida> promociones = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "transicion_publicacion", joinColumns = @JoinColumn(name = "publicacion_id"))
    @OrderColumn(name = "orden")
    public List<TransicionEmbebida> historial = new ArrayList<>();
}
