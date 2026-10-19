package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import java.time.Clock;
import java.time.Instant;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.BilleteraRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.billetera.Billetera;
import ar.edu.uade.skillswap.dominio.billetera.TipoMovimiento;
import ar.edu.uade.skillswap.dominio.cancelacion.FabricaPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.contratacion.ResultadoCancelacion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class CancelarContratacion {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final PublicacionRepositorio publicaciones;
    private final BilleteraRepositorio billeteras;
    private final FabricaPoliticaCancelacion fabricaPoliticas;
    private final Clock reloj;

    public CancelarContratacion(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                                PublicacionRepositorio publicaciones, BilleteraRepositorio billeteras,
                                FabricaPoliticaCancelacion fabricaPoliticas, Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.publicaciones = publicaciones;
        this.billeteras = billeteras;
        this.fabricaPoliticas = fabricaPoliticas;
        this.reloj = reloj;
    }

    /**
     * Operación crítica: el reintegro, el pago al proveedor y la liberación del cupo
     * se confirman juntos, aplicando la política elegida por el autor de la publicación.
     */
    public ContratacionVista ejecutar(CancelarComando comando) {
        return transaccion.ejecutar(() -> {
            Instant ahora = reloj.instant();
            Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
            Publicacion publicacion = publicaciones.obtenerPorId(contratacion.getPublicacionId());
            FranjaHoraria franja = contratacion.getFranja();
            ResultadoCancelacion resultado = contratacion.cancelar(ContextoAccion.de(comando.actor(), ahora),
                    fabricaPoliticas.crear(contratacion.getTipoPoliticaCancelacion()), comando.motivo());
            String referencia = "contratación " + contratacion.getId();
            if (!resultado.reintegroCliente().esCero()) {
                Billetera cliente = billeteras.obtenerPorUsuario(contratacion.getClienteId());
                cliente.acreditar(resultado.reintegroCliente(), TipoMovimiento.REINTEGRO, referencia, ahora);
                billeteras.guardar(cliente);
            }
            if (!resultado.pagoProveedor().esCero()) {
                Billetera proveedor = billeteras.obtenerPorUsuario(contratacion.getProveedorId());
                proveedor.acreditar(resultado.pagoProveedor(), TipoMovimiento.COBRO, referencia, ahora);
                billeteras.guardar(proveedor);
            }
            publicacion.liberarCupo(franja);
            publicaciones.guardar(publicacion);
            Contratacion guardada = contrataciones.guardar(contratacion);
            return ResultadoTransaccion.de(ContratacionVista.de(guardada), guardada.extraerEventos());
        });
    }
}
