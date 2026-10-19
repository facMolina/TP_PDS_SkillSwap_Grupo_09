package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import java.time.Clock;
import java.time.Instant;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.BilleteraRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.dominio.billetera.Billetera;
import ar.edu.uade.skillswap.dominio.billetera.TipoMovimiento;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.error.SaldoInsuficiente;

public class PagarContratacion {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final BilleteraRepositorio billeteras;
    private final Clock reloj;

    public PagarContratacion(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                             BilleteraRepositorio billeteras, Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.billeteras = billeteras;
        this.reloj = reloj;
    }

    /**
     * Operación crítica: el saldo se verifica antes de cambiar el estado y el débito se aplica
     * al final, de modo que un pago rechazado no deja saldos ni estado modificados.
     */
    public ContratacionVista ejecutar(ContratacionComando comando) {
        return transaccion.ejecutar(() -> {
            Instant ahora = reloj.instant();
            Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
            Billetera billetera = billeteras.obtenerPorUsuario(contratacion.getClienteId());
            if (!billetera.tieneSaldoPara(contratacion.getPrecioAcordado())) {
                throw new SaldoInsuficiente("El saldo no alcanza para pagar la contratación");
            }
            contratacion.confirmarPago(ContextoAccion.de(comando.actor(), ahora));
            billetera.debitar(contratacion.getPrecioAcordado(), TipoMovimiento.PAGO,
                    "contratación " + contratacion.getId(), ahora);
            billeteras.guardar(billetera);
            Contratacion guardada = contrataciones.guardar(contratacion);
            return ResultadoTransaccion.de(ContratacionVista.de(guardada), guardada.extraerEventos());
        });
    }
}
