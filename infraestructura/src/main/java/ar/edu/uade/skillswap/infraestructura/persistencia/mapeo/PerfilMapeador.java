package ar.edu.uade.skillswap.infraestructura.persistencia.mapeo;

import java.time.ZoneId;
import java.util.List;

import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.Reputacion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.CanalPreferido;
import ar.edu.uade.skillswap.dominio.perfil.EstadoPerfil;
import ar.edu.uade.skillswap.dominio.perfil.InstantaneaPerfil;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.PerfilEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.PeriodoEmbebido;
import jakarta.persistence.EntityManager;

public final class PerfilMapeador {

    private PerfilMapeador() {
    }

    public static Perfil aDominio(PerfilEntidad e) {
        List<FranjaHoraria> disponibilidad =
                e.disponibilidad.stream().map(p -> new FranjaHoraria(p.inicio, p.fin)).toList();
        return Perfil.reconstruir(new InstantaneaPerfil(e.id, new UsuarioId(e.usuarioId), TipoPerfil.valueOf(e.tipo),
                e.nombreVisible, e.biografia, ZoneId.of(e.zonaHoraria), e.email,
                CanalPreferido.valueOf(e.canalPreferido), CatalogoMapeador.aHabilidades(e.habilidades),
                CatalogoMapeador.aHabilidades(e.intereses), disponibilidad,
                new Reputacion(e.sumaPuntajes, e.cantidadCalificaciones), EstadoPerfil.valueOf(e.estado),
                e.fechaBaja));
    }

    public static PerfilEntidad nueva(Perfil p) {
        PerfilEntidad e = new PerfilEntidad();
        e.usuarioId = p.getUsuarioId().valor();
        e.tipo = p.getTipo().name();
        e.email = p.getEmail();
        return e;
    }

    public static void copiar(Perfil p, PerfilEntidad e, EntityManager entityManager) {
        e.nombreVisible = p.getNombreVisible();
        e.biografia = p.getBiografia();
        e.zonaHoraria = p.getZonaHoraria().getId();
        e.canalPreferido = p.getCanalPreferido().name();
        e.sumaPuntajes = p.getReputacion().sumaPuntajes();
        e.cantidadCalificaciones = p.getReputacion().cantidad();
        e.estado = p.getEstado().name();
        e.fechaBaja = p.getFechaBaja();
        CatalogoMapeador.sincronizar(e.habilidades, p.getHabilidades(), entityManager);
        CatalogoMapeador.sincronizar(e.intereses, p.getIntereses(), entityManager);
        e.disponibilidad.clear();
        p.getDisponibilidad().forEach(f -> e.disponibilidad.add(new PeriodoEmbebido(f.inicio(), f.fin())));
    }
}
