package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record ReprogramarComando(UsuarioId actor, Long contratacionId, FranjaHoraria nuevaFranja) {
}
