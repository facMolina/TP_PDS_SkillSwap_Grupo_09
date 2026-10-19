package ar.edu.uade.skillswap.dominio.billetera;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.comun.Identificable;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public class MovimientoCreditos implements Identificable {

    private Long id;
    private final TipoMovimiento tipo;
    private final SkillCoins monto;
    private final SkillCoins saldoResultante;
    private final String referencia;
    private final Instant fecha;

    public MovimientoCreditos(TipoMovimiento tipo, SkillCoins monto, SkillCoins saldoResultante, String referencia,
                              Instant fecha) {
        this.tipo = tipo;
        this.monto = monto;
        this.saldoResultante = saldoResultante;
        this.referencia = referencia;
        this.fecha = fecha;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void asignarId(Long id) {
        this.id = id;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public SkillCoins getMonto() {
        return monto;
    }

    public SkillCoins getSaldoResultante() {
        return saldoResultante;
    }

    public String getReferencia() {
        return referencia;
    }

    public Instant getFecha() {
        return fecha;
    }
}
