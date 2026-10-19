package ar.edu.uade.skillswap.infraestructura.persistencia.mapeo;

import ar.edu.uade.skillswap.dominio.billetera.Billetera;
import ar.edu.uade.skillswap.dominio.billetera.MovimientoCreditos;
import ar.edu.uade.skillswap.dominio.billetera.TipoMovimiento;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.BilleteraEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.MovimientoCreditoEntidad;

public final class BilleteraMapeador {

    private BilleteraMapeador() {
    }

    public static Billetera aDominio(BilleteraEntidad e) {
        return Billetera.reconstruir(e.id, new UsuarioId(e.usuarioId), SkillCoins.de(e.saldo), e.version);
    }

    public static BilleteraEntidad nueva(Billetera b) {
        BilleteraEntidad e = new BilleteraEntidad();
        e.usuarioId = b.getUsuarioId().valor();
        return e;
    }

    public static void copiar(Billetera b, BilleteraEntidad e) {
        e.saldo = b.getSaldo().monto();
    }

    public static MovimientoCreditoEntidad aEntidad(MovimientoCreditos m, Long billeteraId) {
        MovimientoCreditoEntidad e = new MovimientoCreditoEntidad();
        e.billeteraId = billeteraId;
        e.tipo = m.getTipo().name();
        e.monto = m.getMonto().monto();
        e.saldoResultante = m.getSaldoResultante().monto();
        e.referencia = m.getReferencia();
        e.fecha = m.getFecha();
        return e;
    }

    public static MovimientoCreditos aDominio(MovimientoCreditoEntidad e) {
        MovimientoCreditos movimiento = new MovimientoCreditos(TipoMovimiento.valueOf(e.tipo), SkillCoins.de(e.monto),
                SkillCoins.de(e.saldoResultante), e.referencia, e.fecha);
        movimiento.asignarId(e.id);
        return movimiento;
    }
}
