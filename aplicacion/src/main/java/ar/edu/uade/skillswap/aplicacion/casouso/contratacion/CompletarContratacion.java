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
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

public class CompletarContratacion {

    private final EjecutorTransaccional transaccion;
    private final ContratacionRepositorio contrataciones;
    private final BilleteraRepositorio billeteras;
    private final Clock reloj;

    public CompletarContratacion(EjecutorTransaccional transaccion, ContratacionRepositorio contrataciones,
                                 BilleteraRepositorio billeteras, Clock reloj) {
        this.transaccion = transaccion;
        this.contrataciones = contrataciones;
        this.billeteras = billeteras;
        this.reloj = reloj;
    }

    public ContratacionVista ejecutar(ContratacionComando comando) {
        return transaccion.ejecutar(() -> {
            Instant ahora = reloj.instant();
            Contratacion contratacion = contrataciones.obtenerPorId(comando.contratacionId());
            SkillCoins liberado = contratacion.completar(ContextoAccion.de(comando.actor(), ahora));
            Billetera proveedor = billeteras.obtenerPorUsuario(contratacion.getProveedorId());
            proveedor.acreditar(liberado, TipoMovimiento.COBRO, "contratación " + contratacion.getId(), ahora);
            billeteras.guardar(proveedor);
            Contratacion guardada = contrataciones.guardar(contratacion);
            return ResultadoTransaccion.de(ContratacionVista.de(guardada), guardada.extraerEventos());
        });
    }
}
