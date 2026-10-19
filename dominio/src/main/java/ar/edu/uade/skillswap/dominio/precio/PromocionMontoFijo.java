package ar.edu.uade.skillswap.dominio.precio;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public class PromocionMontoFijo extends Promocion {

    private final SkillCoins monto;

    public PromocionMontoFijo(SkillCoins monto, Instant vigenteDesde, Instant vigenteHasta) {
        super(vigenteDesde, vigenteHasta);
        this.monto = monto;
    }

    @Override
    public Precio decorar(Precio precio) {
        return new DescuentoFijo(precio, monto);
    }

    public SkillCoins getMonto() {
        return monto;
    }
}
