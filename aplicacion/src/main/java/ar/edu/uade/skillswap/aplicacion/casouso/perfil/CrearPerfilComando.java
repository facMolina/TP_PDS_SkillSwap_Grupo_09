package ar.edu.uade.skillswap.aplicacion.casouso.perfil;

import java.time.ZoneId;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;

public record CrearPerfilComando(UsuarioId usuarioId, TipoPerfil tipo, String nombreVisible, String email,
                                 ZoneId zonaHoraria) {
}
