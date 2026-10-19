package ar.edu.uade.skillswap.dominio.precio;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public class DescuentoPorcentual extends PrecioDecorador {

    private final int porcentaje;

    public DescuentoPorcentual(Precio precio, int porcentaje) {
        super(precio);
        this.porcentaje = porcentaje;
    }

    @Override
    public SkillCoins calcular() {
        SkillCoins actual = precio.calcular();
        return actual.restarHastaCero(actual.porcentaje(porcentaje));
    }
}
