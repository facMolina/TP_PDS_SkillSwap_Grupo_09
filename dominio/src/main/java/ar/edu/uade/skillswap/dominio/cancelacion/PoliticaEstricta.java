package ar.edu.uade.skillswap.dominio.cancelacion;

import java.time.Duration;
import java.util.List;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public class PoliticaEstricta implements PoliticaCancelacion {

    private final TramosReintegro tramos;

    public PoliticaEstricta(TramosReintegro tramos) {
        this.tramos = tramos;
    }

    public static PoliticaEstricta porDefecto() {
        return new PoliticaEstricta(new TramosReintegro(List.of(
                new TramoReintegro(Duration.ofHours(72), 100),
                new TramoReintegro(Duration.ofHours(48), 50))));
    }

    @Override
    public SkillCoins calcularReintegro(SkillCoins montoPagado, Duration anticipacion) {
        return montoPagado.porcentaje(tramos.porcentajePara(anticipacion));
    }
}
