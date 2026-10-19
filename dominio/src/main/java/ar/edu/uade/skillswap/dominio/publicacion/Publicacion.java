package ar.edu.uade.skillswap.dominio.publicacion;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.HistorialTransiciones;
import ar.edu.uade.skillswap.dominio.comun.Identificable;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.TransicionEstado;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.comun.Versionado;
import ar.edu.uade.skillswap.dominio.error.CupoAgotado;
import ar.edu.uade.skillswap.dominio.error.FranjaNoDisponible;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;
import ar.edu.uade.skillswap.dominio.error.PublicacionIncompleta;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.precio.Precio;
import ar.edu.uade.skillswap.dominio.precio.PrecioBase;
import ar.edu.uade.skillswap.dominio.precio.Promocion;

public class Publicacion implements Identificable, Versionado {

    private Long id;
    private final UsuarioId autorId;
    private final TipoPublicacion tipo;
    private String titulo;
    private String descripcion;
    private Categoria categoria;
    private Modalidad modalidad;
    private int duracionMinutos;
    private int cupo;
    private int cuposTomados;
    private SkillCoins precioBase;
    private Set<Habilidad> etiquetas = new HashSet<>();
    private TipoPoliticaCancelacion tipoPoliticaCancelacion = TipoPoliticaCancelacion.FLEXIBLE;
    private final List<FranjaDisponible> franjas = new ArrayList<>();
    private final List<Promocion> promociones = new ArrayList<>();
    private EstadoPublicacion estadoActual = new EstadoBorrador();
    private final HistorialTransiciones historial;
    private final Instant fechaCreacion;
    private long version;

    private Publicacion(UsuarioId autorId, TipoPublicacion tipo, Instant fechaCreacion) {
        this.autorId = autorId;
        this.tipo = tipo;
        this.fechaCreacion = fechaCreacion;
        this.historial = new HistorialTransiciones();
    }

    private Publicacion(InstantaneaPublicacion i) {
        this.id = i.id();
        this.autorId = i.autorId();
        this.tipo = i.datos().tipo();
        this.fechaCreacion = i.fechaCreacion();
        this.version = i.version();
        this.historial = new HistorialTransiciones(i.historial());
        this.estadoActual = EstadosPublicacion.porNombre(i.estado());
        this.cuposTomados = i.cuposTomados();
        this.franjas.addAll(i.franjas());
        this.promociones.addAll(i.promociones());
        aplicar(i.datos());
    }

    public static Publicacion reconstruir(InstantaneaPublicacion instantanea) {
        return new Publicacion(instantanea);
    }

    public static Publicacion crearBorrador(Perfil autor, DatosPublicacion datos, Instant ahora) {
        if (datos.tipo() == null) {
            throw new ValorInvalido("El tipo de publicación es obligatorio");
        }
        if (!autor.puedePublicar(datos.tipo())) {
            throw new PermisoDenegado("El perfil no puede crear publicaciones de tipo " + datos.tipo());
        }
        Publicacion publicacion = new Publicacion(autor.getUsuarioId(), datos.tipo(), ahora);
        publicacion.aplicar(datos);
        publicacion.historial.registrar(
                new TransicionEstado(ahora, autor.getUsuarioId(), "NINGUNO", "BORRADOR", "Creación"));
        return publicacion;
    }

    public void editar(DatosPublicacion datos, ContextoAccion ctx) {
        exigirAutor(ctx);
        if (!estadoActual.permiteEdicion()) {
            throw new TransicionInvalida("La publicación solo puede editarse en estado BORRADOR");
        }
        if (datos.tipo() != tipo) {
            throw new ValorInvalido("El tipo de publicación no puede modificarse");
        }
        aplicar(datos);
    }

    public void agregarFranja(FranjaHoraria franja, ContextoAccion ctx) {
        exigirAutor(ctx);
        exigirNoFinal();
        boolean seSuperpone = franjas.stream().anyMatch(existente -> existente.getFranja().seSolapaCon(franja));
        if (seSuperpone) {
            throw new ValorInvalido("La franja se superpone con otra ya ofrecida");
        }
        franjas.add(new FranjaDisponible(franja));
    }

    public void agregarPromocion(Promocion promocion, ContextoAccion ctx) {
        exigirAutor(ctx);
        exigirNoFinal();
        promociones.add(promocion);
    }

    public void publicar(ContextoAccion ctx) {
        exigirAutor(ctx);
        estadoActual.publicar(this, ctx);
    }

    public void pausar(ContextoAccion ctx) {
        exigirAutor(ctx);
        estadoActual.pausar(this, ctx);
    }

    public void reanudar(ContextoAccion ctx) {
        exigirAutor(ctx);
        estadoActual.reanudar(this, ctx);
    }

    public void cerrar(ContextoAccion ctx) {
        exigirAutor(ctx);
        estadoActual.cerrar(this, ctx);
    }

    public void cancelar(ContextoAccion ctx, String motivo) {
        exigirAutor(ctx);
        if (motivo == null || motivo.isBlank()) {
            throw new ValorInvalido("El motivo de cancelación es obligatorio");
        }
        estadoActual.cancelar(this, ctx, motivo);
    }

    public boolean admiteSolicitudes() {
        return estadoActual.admiteSolicitudes();
    }

    public boolean esVisiblePara(UsuarioId usuario) {
        return estadoActual.esVisiblePara(this, usuario);
    }

    public boolean tieneFranjaLibre(FranjaHoraria franja) {
        return franjas.stream().anyMatch(disponible -> disponible.coincideCon(franja) && !disponible.estaOcupada());
    }

    public void reservarCupo(FranjaHoraria franja) {
        if (cuposTomados >= cupo) {
            throw new CupoAgotado("La publicación no tiene cupos disponibles");
        }
        buscarFranja(franja).reservar();
        cuposTomados++;
    }

    public void liberarCupo(FranjaHoraria franja) {
        buscarFranja(franja).liberar();
        if (cuposTomados > 0) {
            cuposTomados--;
        }
    }

    public void cambiarFranja(FranjaHoraria anterior, FranjaHoraria nueva) {
        buscarFranja(nueva).reservar();
        buscarFranja(anterior).liberar();
    }

    public SkillCoins calcularPrecio(Instant ahora) {
        Precio precio = new PrecioBase(precioBase);
        for (Promocion promocion : promociones) {
            if (promocion.estaVigente(ahora)) {
                precio = promocion.decorar(precio);
            }
        }
        return precio.calcular();
    }

    public void cambiarEstado(EstadoPublicacion nuevo, ContextoAccion ctx, String motivo) {
        historial.registrar(
                new TransicionEstado(ctx.ahora(), ctx.actor(), estadoActual.getNombre(), nuevo.getNombre(), motivo));
        estadoActual = nuevo;
    }

    void exigirCompleta() {
        List<String> faltantes = new ArrayList<>();
        if (titulo == null || titulo.isBlank()) {
            faltantes.add("título");
        }
        if (categoria == null) {
            faltantes.add("categoría");
        }
        if (precioBase == null) {
            faltantes.add("precio");
        }
        if (franjas.isEmpty()) {
            faltantes.add("al menos una franja de disponibilidad");
        }
        if (cupo < 1) {
            faltantes.add("cupo");
        }
        if (!faltantes.isEmpty()) {
            throw new PublicacionIncompleta("La publicación está incompleta: falta " + String.join(", ", faltantes));
        }
    }

    private void aplicar(DatosPublicacion datos) {
        titulo = datos.titulo();
        descripcion = datos.descripcion();
        categoria = datos.categoria();
        modalidad = datos.modalidad();
        duracionMinutos = datos.duracionMinutos();
        cupo = datos.cupo();
        precioBase = datos.precioBase();
        etiquetas = datos.etiquetas() == null ? new HashSet<>() : new HashSet<>(datos.etiquetas());
        if (datos.tipoPoliticaCancelacion() != null) {
            tipoPoliticaCancelacion = datos.tipoPoliticaCancelacion();
        }
    }

    private void exigirAutor(ContextoAccion ctx) {
        if (!autorId.equals(ctx.actor())) {
            throw new PermisoDenegado("Solo el autor puede modificar la publicación");
        }
    }

    private void exigirNoFinal() {
        if (estadoActual.esFinal()) {
            throw new TransicionInvalida("La publicación está en estado " + estadoActual.getNombre());
        }
    }

    private FranjaDisponible buscarFranja(FranjaHoraria franja) {
        return franjas.stream()
                .filter(disponible -> disponible.coincideCon(franja))
                .findFirst()
                .orElseThrow(() -> new FranjaNoDisponible("La franja no pertenece a la publicación"));
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void asignarId(Long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void fijarVersion(long version) {
        this.version = version;
    }

    public UsuarioId getAutorId() {
        return autorId;
    }

    public TipoPublicacion getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public int getCupo() {
        return cupo;
    }

    public int getCuposTomados() {
        return cuposTomados;
    }

    public SkillCoins getPrecioBase() {
        return precioBase;
    }

    public Set<Habilidad> getEtiquetas() {
        return Collections.unmodifiableSet(etiquetas);
    }

    public TipoPoliticaCancelacion getTipoPoliticaCancelacion() {
        return tipoPoliticaCancelacion;
    }

    public List<FranjaDisponible> getFranjas() {
        return Collections.unmodifiableList(franjas);
    }

    public List<Promocion> getPromociones() {
        return Collections.unmodifiableList(promociones);
    }

    public String getNombreEstado() {
        return estadoActual.getNombre();
    }

    public HistorialTransiciones getHistorial() {
        return historial;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
