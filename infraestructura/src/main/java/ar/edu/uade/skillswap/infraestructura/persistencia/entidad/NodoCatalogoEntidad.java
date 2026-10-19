package ar.edu.uade.skillswap.infraestructura.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "nodo_catalogo")
public class NodoCatalogoEntidad {

    public static final String CATEGORIA = "CATEGORIA";
    public static final String HABILIDAD = "HABILIDAD";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String tipo;

    @Column(nullable = false)
    public String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    public NodoCatalogoEntidad padre;

    public boolean esCategoria() {
        return CATEGORIA.equals(tipo);
    }
}
