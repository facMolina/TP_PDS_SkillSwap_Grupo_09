package ar.edu.uade.skillswap.aplicacion.puerto;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public interface EnviadorPush {

    void enviarPush(UsuarioId destinatario, String titulo, String cuerpo);
}
