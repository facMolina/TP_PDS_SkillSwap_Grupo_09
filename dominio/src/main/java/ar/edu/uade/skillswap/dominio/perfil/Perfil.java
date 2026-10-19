package ar.edu.uade.skillswap.dominio.perfil;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.Identificable;
import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.comun.Reputacion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;

public class Perfil implements Identificable {

    private Long id;
    private final UsuarioId usuarioId;
    private final TipoPerfil tipo;
    private String nombreVisible;
    private String biografia;
    private ZoneId zonaHoraria;
    private final String email;
    private CanalPreferido canalPreferido = CanalPreferido.INTERNA;
    private Set<Habilidad> habilidades = new HashSet<>();
    private Set<Habilidad> intereses = new HashSet<>();
    private List<FranjaHoraria> disponibilidad = new ArrayList<>();
    private Reputacion reputacion = Reputacion.SIN_CALIFICACIONES;
    private EstadoPerfil estado = EstadoPerfil.ACTIVO;
    private Instant fechaBaja;

    private Perfil(UsuarioId usuarioId, TipoPerfil tipo, String nombreVisible, String email, ZoneId zonaHoraria) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.nombreVisible = nombreVisible;
        this.email = email;
        this.zonaHoraria = zonaHoraria;
    }

    public static Perfil crear(UsuarioId usuarioId, TipoPerfil tipo, String nombreVisible, String email,
                               ZoneId zonaHoraria) {
        if (nombreVisible == null || nombreVisible.isBlank()) {
            throw new ValorInvalido("El nombre visible es obligatorio");
        }
        if (zonaHoraria == null) {
            throw new ValorInvalido("La zona horaria es obligatoria");
        }
        return new Perfil(usuarioId, tipo, nombreVisible, email, zonaHoraria);
    }

    public static Perfil reconstruir(InstantaneaPerfil i) {
        Perfil perfil = new Perfil(i.usuarioId(), i.tipo(), i.nombreVisible(), i.email(), i.zonaHoraria());
        perfil.id = i.id();
        perfil.biografia = i.biografia();
        perfil.canalPreferido = i.canalPreferido();
        perfil.habilidades = new HashSet<>(i.habilidades());
        perfil.intereses = new HashSet<>(i.intereses());
        perfil.disponibilidad = new ArrayList<>(i.disponibilidad());
        perfil.reputacion = i.reputacion();
        perfil.estado = i.estado();
        perfil.fechaBaja = i.fechaBaja();
        return perfil;
    }

    public void actualizarDatos(String nombreVisible, String biografia, ZoneId zonaHoraria) {
        exigirActivo();
        if (nombreVisible == null || nombreVisible.isBlank()) {
            throw new ValorInvalido("El nombre visible es obligatorio");
        }
        this.nombreVisible = nombreVisible;
        this.biografia = biografia;
        this.zonaHoraria = zonaHoraria;
    }

    public void definirHabilidades(Set<Habilidad> habilidades) {
        exigirActivo();
        this.habilidades = new HashSet<>(habilidades);
    }

    public void definirIntereses(Set<Habilidad> intereses) {
        exigirActivo();
        this.intereses = new HashSet<>(intereses);
    }

    public void definirDisponibilidad(List<FranjaHoraria> franjas) {
        exigirActivo();
        this.disponibilidad = new ArrayList<>(franjas);
    }

    public void elegirCanal(CanalPreferido canal) {
        exigirActivo();
        this.canalPreferido = canal;
    }

    public void recibirCalificacion(Puntaje puntaje) {
        reputacion = reputacion.incorporar(puntaje);
    }

    public void darDeBaja(Instant ahora) {
        exigirActivo();
        estado = EstadoPerfil.BAJA;
        fechaBaja = ahora;
    }

    public boolean estaActivo() {
        return estado == EstadoPerfil.ACTIVO;
    }

    public boolean puedePublicar(TipoPublicacion tipoPublicacion) {
        return estaActivo() && tipo.puedePublicar(tipoPublicacion);
    }

    public Set<Habilidad> habilidadesEInteresesCombinados() {
        Set<Habilidad> combinadas = new HashSet<>(habilidades);
        combinadas.addAll(intereses);
        return combinadas;
    }

    private void exigirActivo() {
        if (!estaActivo()) {
            throw new TransicionInvalida("El perfil está dado de baja");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void asignarId(Long id) {
        this.id = id;
    }

    public UsuarioId getUsuarioId() {
        return usuarioId;
    }

    public TipoPerfil getTipo() {
        return tipo;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    public String getBiografia() {
        return biografia;
    }

    public ZoneId getZonaHoraria() {
        return zonaHoraria;
    }

    public String getEmail() {
        return email;
    }

    public CanalPreferido getCanalPreferido() {
        return canalPreferido;
    }

    public Set<Habilidad> getHabilidades() {
        return Collections.unmodifiableSet(habilidades);
    }

    public Set<Habilidad> getIntereses() {
        return Collections.unmodifiableSet(intereses);
    }

    public List<FranjaHoraria> getDisponibilidad() {
        return Collections.unmodifiableList(disponibilidad);
    }

    public Reputacion getReputacion() {
        return reputacion;
    }

    public EstadoPerfil getEstado() {
        return estado;
    }

    public Instant getFechaBaja() {
        return fechaBaja;
    }
}
