package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

final class EstadosPublicacion {

    private EstadosPublicacion() {
    }

    static EstadoPublicacion porNombre(String nombre) {
        return switch (nombre) {
            case "BORRADOR" -> new EstadoBorrador();
            case "PUBLICADA" -> new EstadoPublicada();
            case "PAUSADA" -> new EstadoPausada();
            case "CERRADA" -> new EstadoCerrada();
            case "CANCELADA" -> new EstadoCancelada();
            default -> throw new ValorInvalido("Estado de publicación desconocido: " + nombre);
        };
    }
}
