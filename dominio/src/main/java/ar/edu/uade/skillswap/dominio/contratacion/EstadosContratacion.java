package ar.edu.uade.skillswap.dominio.contratacion;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

final class EstadosContratacion {

    private EstadosContratacion() {
    }

    static EstadoContratacion porNombre(String nombre) {
        return switch (nombre) {
            case "PENDIENTE" -> new EstadoPendiente();
            case "ACEPTADA" -> new EstadoAceptada();
            case "CONFIRMADA" -> new EstadoConfirmada();
            case "EN_CURSO" -> new EstadoEnCurso();
            case "COMPLETADA" -> new EstadoCompletada();
            case "RECHAZADA" -> new EstadoRechazada();
            case "CANCELADA" -> new EstadoCancelada();
            default -> throw new ValorInvalido("Estado de contratación desconocido: " + nombre);
        };
    }
}
