package ar.edu.uade.skillswap.aplicacion.casouso.perfil;

import java.time.ZoneId;
import java.util.Set;

import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.CanalPreferido;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;

public record PerfilVista(UsuarioId usuarioId, TipoPerfil tipo, String nombreVisible, String biografia,
                          ZoneId zonaHoraria, CanalPreferido canalPreferido, Set<Habilidad> habilidades,
                          Set<Habilidad> intereses, double reputacion, int cantidadCalificaciones, boolean activo) {

    public static PerfilVista de(Perfil perfil) {
        return new PerfilVista(perfil.getUsuarioId(), perfil.getTipo(), perfil.getNombreVisible(),
                perfil.getBiografia(), perfil.getZonaHoraria(), perfil.getCanalPreferido(), perfil.getHabilidades(),
                perfil.getIntereses(), perfil.getReputacion().promedio(), perfil.getReputacion().cantidad(),
                perfil.estaActivo());
    }
}
