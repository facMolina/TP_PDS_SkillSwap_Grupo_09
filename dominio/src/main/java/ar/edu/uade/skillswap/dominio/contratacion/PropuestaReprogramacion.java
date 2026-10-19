package ar.edu.uade.skillswap.dominio.contratacion;

import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public class PropuestaReprogramacion {

    private final UsuarioId propuestaPor;
    private final FranjaHoraria nuevaFranja;
    private EstadoPropuesta estado = EstadoPropuesta.PENDIENTE;

    public PropuestaReprogramacion(UsuarioId propuestaPor, FranjaHoraria nuevaFranja) {
        this.propuestaPor = propuestaPor;
        this.nuevaFranja = nuevaFranja;
    }

    public PropuestaReprogramacion(UsuarioId propuestaPor, FranjaHoraria nuevaFranja, EstadoPropuesta estado) {
        this.propuestaPor = propuestaPor;
        this.nuevaFranja = nuevaFranja;
        this.estado = estado;
    }

    public void aceptar() {
        estado = EstadoPropuesta.ACEPTADA;
    }

    public void rechazar() {
        estado = EstadoPropuesta.RECHAZADA;
    }

    public boolean estaPendiente() {
        return estado == EstadoPropuesta.PENDIENTE;
    }

    public UsuarioId getPropuestaPor() {
        return propuestaPor;
    }

    public FranjaHoraria getNuevaFranja() {
        return nuevaFranja;
    }

    public EstadoPropuesta getEstado() {
        return estado;
    }
}
