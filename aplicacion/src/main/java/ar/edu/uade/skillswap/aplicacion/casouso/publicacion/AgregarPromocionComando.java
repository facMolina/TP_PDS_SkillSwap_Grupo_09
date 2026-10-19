package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record AgregarPromocionComando(UsuarioId actor, Long publicacionId, TipoPromocion tipo, long valor,
                                      Instant vigenteDesde, Instant vigenteHasta) {
}
