package ar.edu.uade.skillswap.dominio.perfil;

import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;

public enum TipoPerfil {
    PERSONA,
    ORGANIZACION;

    public boolean puedePublicar(TipoPublicacion tipo) {
        return switch (this) {
            case PERSONA -> tipo != TipoPublicacion.DESAFIO;
            case ORGANIZACION -> tipo == TipoPublicacion.DESAFIO;
        };
    }
}
