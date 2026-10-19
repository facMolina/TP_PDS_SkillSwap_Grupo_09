package ar.edu.uade.skillswap.dominio.notificacion;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.comun.Identificable;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;

public class Notificacion implements Identificable {

    private Long id;
    private final UsuarioId destinatarioId;
    private final TipoEvento tipo;
    private final String mensaje;
    private final Instant fecha;
    private boolean leida;

    public Notificacion(UsuarioId destinatarioId, TipoEvento tipo, String mensaje, Instant fecha) {
        this.destinatarioId = destinatarioId;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public static Notificacion reconstruir(Long id, UsuarioId destinatarioId, TipoEvento tipo, String mensaje,
                                           Instant fecha, boolean leida) {
        Notificacion notificacion = new Notificacion(destinatarioId, tipo, mensaje, fecha);
        notificacion.id = id;
        notificacion.leida = leida;
        return notificacion;
    }

    public void marcarLeida(UsuarioId usuario) {
        if (!perteneceA(usuario)) {
            throw new PermisoDenegado("La notificación pertenece a otro usuario");
        }
        leida = true;
    }

    public boolean perteneceA(UsuarioId usuario) {
        return destinatarioId.equals(usuario);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void asignarId(Long id) {
        this.id = id;
    }

    public UsuarioId getDestinatarioId() {
        return destinatarioId;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Instant getFecha() {
        return fecha;
    }

    public boolean estaLeida() {
        return leida;
    }
}
