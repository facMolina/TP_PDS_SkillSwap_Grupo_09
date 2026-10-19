package ar.edu.uade.skillswap.dominio.comun;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

public record SkillCoins(long monto) {

    public static final SkillCoins CERO = new SkillCoins(0);

    public SkillCoins {
        if (monto < 0) {
            throw new ValorInvalido("Los SkillCoins no pueden ser negativos");
        }
    }

    public static SkillCoins de(long monto) {
        return new SkillCoins(monto);
    }

    public SkillCoins sumar(SkillCoins otro) {
        return new SkillCoins(monto + otro.monto);
    }

    public SkillCoins restar(SkillCoins otro) {
        if (esMenorQue(otro)) {
            throw new ValorInvalido("La resta produciría un monto negativo");
        }
        return new SkillCoins(monto - otro.monto);
    }

    public SkillCoins restarHastaCero(SkillCoins otro) {
        return new SkillCoins(Math.max(0, monto - otro.monto));
    }

    public SkillCoins porcentaje(int valor) {
        if (valor < 0 || valor > 100) {
            throw new ValorInvalido("El porcentaje debe estar entre 0 y 100");
        }
        return new SkillCoins(monto * valor / 100);
    }

    public boolean esMenorQue(SkillCoins otro) {
        return monto < otro.monto;
    }

    public boolean esCero() {
        return monto == 0;
    }
}
