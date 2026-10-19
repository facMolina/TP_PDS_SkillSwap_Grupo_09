package ar.edu.uade.skillswap.aplicacion.casouso.billetera;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.billetera.MovimientoCreditos;
import ar.edu.uade.skillswap.dominio.billetera.TipoMovimiento;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public record MovimientoVista(TipoMovimiento tipo, SkillCoins monto, SkillCoins saldoResultante, String referencia,
                              Instant fecha) {

    public static MovimientoVista de(MovimientoCreditos movimiento) {
        return new MovimientoVista(movimiento.getTipo(), movimiento.getMonto(), movimiento.getSaldoResultante(),
                movimiento.getReferencia(), movimiento.getFecha());
    }
}
