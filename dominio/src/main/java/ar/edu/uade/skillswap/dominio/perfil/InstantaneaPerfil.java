package ar.edu.uade.skillswap.dominio.perfil;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.Reputacion;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record InstantaneaPerfil(Long id, UsuarioId usuarioId, TipoPerfil tipo, String nombreVisible, String biografia,
                                ZoneId zonaHoraria, String email, CanalPreferido canalPreferido,
                                Set<Habilidad> habilidades, Set<Habilidad> intereses,
                                List<FranjaHoraria> disponibilidad, Reputacion reputacion, EstadoPerfil estado,
                                Instant fechaBaja) {
}
