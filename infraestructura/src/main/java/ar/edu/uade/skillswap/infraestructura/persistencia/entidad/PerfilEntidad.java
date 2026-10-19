package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "perfil")
public class PerfilEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    public String usuarioId;

    @Column(nullable = false, updatable = false)
    public String tipo;

    @Column(name = "nombre_visible", nullable = false)
    public String nombreVisible;

    public String biografia;

    @Column(name = "zona_horaria", nullable = false)
    public String zonaHoraria;

    @Column(updatable = false)
    public String email;

    @Column(name = "canal_preferido", nullable = false)
    public String canalPreferido;

    @Column(name = "suma_puntajes", nullable = false)
    public long sumaPuntajes;

    @Column(name = "cantidad_calificaciones", nullable = false)
    public int cantidadCalificaciones;

    @Column(nullable = false)
    public String estado;

    @Column(name = "fecha_baja")
    public Instant fechaBaja;

    @Version
    public long version;

    @ManyToMany
    @JoinTable(name = "perfil_habilidad", joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "habilidad_id"))
    public Set<NodoCatalogoEntidad> habilidades = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "perfil_interes", joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "habilidad_id"))
    public Set<NodoCatalogoEntidad> intereses = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "perfil_disponibilidad", joinColumns = @JoinColumn(name = "perfil_id"))
    @OrderColumn(name = "orden")
    public List<PeriodoEmbebido> disponibilidad = new ArrayList<>();
}
