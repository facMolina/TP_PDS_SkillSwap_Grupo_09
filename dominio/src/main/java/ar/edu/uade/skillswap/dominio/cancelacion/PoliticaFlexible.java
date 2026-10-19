package ar.edu.uade.skillswap.dominio.cancelacion;

import java.time.Duration;
import java.util.List;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public class PoliticaFlexible implements PoliticaCancelacion {

    private final TramosReintegro tramos;

    public PoliticaFlexible(TramosReintegro tramos) {
        this.tramos = tramos;
    }

    public static PoliticaFlexible porDefecto() {
        return new PoliticaFlexible(new TramosReintegro(List.of(
                new TramoReintegro(Duration.ofHours(24), 100),
                new TramoReintegro(Duration.ofHours(12), 50))));
    }

    @Override
    public SkillCoins calcularReintegro(SkillCoins montoPagado, Duration anticipacion) {
        return montoPagado.porcentaje(tramos.porcentajePara(anticipacion));
    }
}
