package ar.edu.uade.skillswap.aplicacion.casouso.billetera;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.puerto.BilleteraRepositorio;
import ar.edu.uade.skillswap.dominio.billetera.Billetera;

public class ConsultarBilletera {

    private final EjecutorTransaccional transaccion;
    private final BilleteraRepositorio billeteras;

    public ConsultarBilletera(EjecutorTransaccional transaccion, BilleteraRepositorio billeteras) {
        this.transaccion = transaccion;
        this.billeteras = billeteras;
    }

    public BilleteraVista ejecutar(ConsultaBilletera consulta) {
        return transaccion.consultar(() -> {
            Billetera billetera = billeteras.obtenerPorUsuario(consulta.actor());
            return new BilleteraVista(billetera.getSaldo(),
                    billeteras.listarMovimientos(consulta.actor(), consulta.pagina()).transformar(MovimientoVista::de));
        });
    }
}
