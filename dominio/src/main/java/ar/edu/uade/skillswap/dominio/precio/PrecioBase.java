package ar.edu.uade.skillswap.dominio.precio;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public class PrecioBase implements Precio {

    private final SkillCoins monto;

    public PrecioBase(SkillCoins monto) {
        this.monto = monto;
    }

    @Override
    public SkillCoins calcular() {
        return monto;
    }
}
