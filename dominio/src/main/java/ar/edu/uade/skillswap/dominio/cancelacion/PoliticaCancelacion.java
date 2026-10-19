package ar.edu.uade.skillswap.dominio.cancelacion;

import java.time.Duration;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public interface PoliticaCancelacion {

    SkillCoins calcularReintegro(SkillCoins montoPagado, Duration anticipacion);
}
