package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record SolicitarComando(UsuarioId actor, Long publicacionId, String mensaje, String objetivo,
                               FranjaHoraria franja) {
}
