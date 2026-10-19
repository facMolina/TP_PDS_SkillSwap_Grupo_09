package ar.edu.uade.skillswap.dominio.precio;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public class DescuentoFijo extends PrecioDecorador {

    private final SkillCoins monto;

    public DescuentoFijo(Precio precio, SkillCoins monto) {
        super(precio);
        this.monto = monto;
    }

    @Override
    public SkillCoins calcular() {
        return precio.calcular().restarHastaCero(monto);
    }
}
