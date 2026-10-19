package ar.edu.uade.skillswap.infraestructura.persistencia.mapeo;

import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.contratacion.EstadoPropuesta;
import ar.edu.uade.skillswap.dominio.contratacion.InstantaneaContratacion;
import ar.edu.uade.skillswap.dominio.contratacion.PropuestaReprogramacion;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.ContratacionEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.PropuestaEmbebida;

public final class ContratacionMapeador {

    private ContratacionMapeador() {
    }

    public static Contratacion aDominio(ContratacionEntidad e) {
        return Contratacion.reconstruir(new InstantaneaContratacion(e.id, e.publicacionId,
                new UsuarioId(e.clienteId), new UsuarioId(e.proveedorId), e.mensaje, e.objetivo,
                new FranjaHoraria(e.franjaInicio, e.franjaFin), SkillCoins.de(e.precioAcordado),
                SkillCoins.de(e.montoRetenido), TipoPoliticaCancelacion.valueOf(e.politicaCancelacion),
                e.fechaSolicitud, e.fechaAceptacion, e.fechaCompletada, aPropuesta(e.propuesta), e.estado,
                TransicionMapeador.aDominio(e.historial)));
    }

    public static ContratacionEntidad nueva(Contratacion c) {
        ContratacionEntidad e = new ContratacionEntidad();
        e.publicacionId = c.getPublicacionId();
        e.clienteId = c.getClienteId().valor();
        e.proveedorId = c.getProveedorId().valor();
        e.mensaje = c.getMensaje();
        e.objetivo = c.getObjetivo();
        e.precioAcordado = c.getPrecioAcordado().monto();
        e.politicaCancelacion = c.getTipoPoliticaCancelacion().name();
        e.fechaSolicitud = c.getFechaSolicitud();
        return e;
    }

    public static void copiar(Contratacion c, ContratacionEntidad e) {
        e.franjaInicio = c.getFranja().inicio();
        e.franjaFin = c.getFranja().fin();
        e.montoRetenido = c.getMontoRetenido().monto();
        e.fechaAceptacion = c.getFechaAceptacion();
        e.fechaCompletada = c.getFechaCompletada();
        e.estado = c.getNombreEstado();
        e.propuesta = aEmbebida(c.getPropuesta());
        TransicionMapeador.copiar(c.getHistorial().getTransiciones(), e.historial);
    }

    private static PropuestaReprogramacion aPropuesta(PropuestaEmbebida e) {
        if (e == null || e.propuestaPor == null) {
            return null;
        }
        return new PropuestaReprogramacion(new UsuarioId(e.propuestaPor), new FranjaHoraria(e.inicio, e.fin),
                EstadoPropuesta.valueOf(e.estado));
    }

    private static PropuestaEmbebida aEmbebida(PropuestaReprogramacion propuesta) {
        if (propuesta == null) {
            return null;
        }
        PropuestaEmbebida e = new PropuestaEmbebida();
        e.propuestaPor = propuesta.getPropuestaPor().valor();
        e.inicio = propuesta.getNuevaFranja().inicio();
        e.fin = propuesta.getNuevaFranja().fin();
        e.estado = propuesta.getEstado().name();
        return e;
    }
}
