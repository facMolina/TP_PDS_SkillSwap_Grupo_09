package ar.edu.uade.skillswap.aplicacion.comun;

import java.time.Duration;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public record ConfiguracionAplicacion(SkillCoins saldoInicial, Duration plazoPago, int maximoHistorialResumido) {

    public static ConfiguracionAplicacion porDefecto() {
        return new ConfiguracionAplicacion(SkillCoins.de(1000), Duration.ofHours(48), 10);
    }
}
