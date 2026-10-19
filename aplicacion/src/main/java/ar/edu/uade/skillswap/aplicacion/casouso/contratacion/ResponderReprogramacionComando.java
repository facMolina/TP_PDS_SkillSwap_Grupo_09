package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record ResponderReprogramacionComando(UsuarioId actor, Long contratacionId, boolean acepta) {
}
