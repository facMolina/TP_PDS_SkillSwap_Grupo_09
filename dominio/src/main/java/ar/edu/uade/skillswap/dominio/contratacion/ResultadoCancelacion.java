package ar.edu.uade.skillswap.dominio.contratacion;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public record ResultadoCancelacion(SkillCoins reintegroCliente, SkillCoins pagoProveedor) {

    public static ResultadoCancelacion sinMovimientos() {
        return new ResultadoCancelacion(SkillCoins.CERO, SkillCoins.CERO);
    }
}
