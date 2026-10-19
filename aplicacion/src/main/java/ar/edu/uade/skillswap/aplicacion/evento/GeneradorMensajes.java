package ar.edu.uade.skillswap.aplicacion.evento;

import java.util.EnumMap;
import java.util.Map;

import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;

public class GeneradorMensajes {

    private final Map<TipoEvento, String> plantillas = new EnumMap<>(TipoEvento.class);

    public GeneradorMensajes() {
        plantillas.put(TipoEvento.SOLICITUD_RECIBIDA, "Recibiste una solicitud para la contratación %d");
        plantillas.put(TipoEvento.SOLICITUD_ACEPTADA, "Se aceptó tu solicitud de la contratación %d");
        plantillas.put(TipoEvento.SOLICITUD_RECHAZADA, "Se rechazó tu solicitud de la contratación %d");
        plantillas.put(TipoEvento.PAGO_CONFIRMADO, "Se confirmó el pago de la contratación %d");
        plantillas.put(TipoEvento.REPROGRAMACION_PROPUESTA, "Hay una propuesta de reprogramación para la contratación %d");
        plantillas.put(TipoEvento.REPROGRAMACION_RESUELTA, "Se respondió la reprogramación de la contratación %d");
        plantillas.put(TipoEvento.CONTRATACION_CANCELADA, "Se canceló la contratación %d");
        plantillas.put(TipoEvento.SESION_COMPLETADA, "Se completó la sesión de la contratación %d");
    }

    public String mensajePara(EventoContratacion evento) {
        return plantillas.get(evento.tipo()).formatted(evento.contratacionId());
    }
}
