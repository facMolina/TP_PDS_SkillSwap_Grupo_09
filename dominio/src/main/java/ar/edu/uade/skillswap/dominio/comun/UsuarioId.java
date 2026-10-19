package ar.edu.uade.skillswap.dominio.comun;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

public record UsuarioId(String valor) {

    public static final UsuarioId SISTEMA = new UsuarioId("sistema");

    public UsuarioId {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalido("El identificador de usuario es obligatorio");
        }
    }
}
