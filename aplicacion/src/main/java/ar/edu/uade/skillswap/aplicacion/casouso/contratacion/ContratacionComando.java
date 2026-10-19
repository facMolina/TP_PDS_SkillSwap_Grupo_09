package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record ContratacionComando(UsuarioId actor, Long contratacionId) {
}
