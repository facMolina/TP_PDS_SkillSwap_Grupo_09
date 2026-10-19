package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.util.List;

import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record CrearBorradorComando(UsuarioId actor, DatosPublicacionComando datos, List<FranjaHoraria> franjas) {
}
