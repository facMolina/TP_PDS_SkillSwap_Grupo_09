package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.dominio.contratacion.Comprobante;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;

public class ConsultarComprobante {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;

    public ConsultarComprobante(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
    }

    public Comprobante ejecutar(ContratacionComando comando) {
        return transaccion.consultar(() -> {
            Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
            if (!contratacion.esParticipante(comando.actor())) {
                throw new PermisoDenegado("Solo los participantes pueden consultar el comprobante");
            }
            return contratacion.generarComprobante();
        });
    }
}
