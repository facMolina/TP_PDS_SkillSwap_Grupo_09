package ar.edu.uade.skillswap.aplicacion.casouso.perfil;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.CanalPreferido;

public record ActualizarPerfilComando(UsuarioId actor, String nombreVisible, String biografia, ZoneId zonaHoraria,
                                      Set<Long> habilidadIds, Set<Long> interesIds,
                                      List<FranjaHoraria> disponibilidad, CanalPreferido canal) {
}
