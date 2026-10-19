package ar.edu.uade.skillswap.aplicacion.puerto;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public interface ServicioIdentidad {

    boolean existeUsuario(UsuarioId usuario);
}
