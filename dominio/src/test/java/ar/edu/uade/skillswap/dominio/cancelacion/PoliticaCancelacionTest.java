package ar.edu.uade.skillswap.dominio.cancelacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

class PoliticaCancelacionTest {

    private static final SkillCoins MONTO = SkillCoins.de(100);

    @Test
    void laPoliticaFlexibleReintegraSegunLosTramosPorDefecto() {
        PoliticaCancelacion politica = PoliticaFlexible.porDefecto();

        assertEquals(SkillCoins.de(100), politica.calcularReintegro(MONTO, Duration.ofHours(24)));
        assertEquals(SkillCoins.de(100), politica.calcularReintegro(MONTO, Duration.ofDays(5)));
        assertEquals(SkillCoins.de(50), politica.calcularReintegro(MONTO, Duration.ofHours(23)));
        assertEquals(SkillCoins.de(50), politica.calcularReintegro(MONTO, Duration.ofHours(12)));
        assertEquals(SkillCoins.CERO, politica.calcularReintegro(MONTO, Duration.ofHours(11)));
    }

    @Test
    void laPoliticaEstrictaReintegraSegunLosTramosPorDefecto() {
        PoliticaCancelacion politica = PoliticaEstricta.porDefecto();

        assertEquals(SkillCoins.de(100), politica.calcularReintegro(MONTO, Duration.ofHours(72)));
        assertEquals(SkillCoins.de(50), politica.calcularReintegro(MONTO, Duration.ofHours(60)));
        assertEquals(SkillCoins.de(50), politica.calcularReintegro(MONTO, Duration.ofHours(48)));
        assertEquals(SkillCoins.CERO, politica.calcularReintegro(MONTO, Duration.ofHours(47)));
    }

    @Test
    void lasDosPoliticasDifierenParaLaMismaAnticipacion() {
        Duration anticipacion = Duration.ofHours(30);

        assertEquals(SkillCoins.de(100), PoliticaFlexible.porDefecto().calcularReintegro(MONTO, anticipacion));
        assertEquals(SkillCoins.CERO, PoliticaEstricta.porDefecto().calcularReintegro(MONTO, anticipacion));
    }

    @Test
    void losTramosSonConfigurablesYSeOrdenanSolos() {
        TramosReintegro tramos = new TramosReintegro(List.of(
                new TramoReintegro(Duration.ofHours(1), 25),
                new TramoReintegro(Duration.ofHours(10), 75)));

        assertEquals(75, tramos.porcentajePara(Duration.ofHours(10)));
        assertEquals(25, tramos.porcentajePara(Duration.ofHours(2)));
        assertEquals(0, tramos.porcentajePara(Duration.ofMinutes(30)));
    }

    @Test
    void losTramosExigenAlMenosUnValor() {
        assertThrows(ValorInvalido.class, () -> new TramosReintegro(List.of()));
    }

    @Test
    void laFabricaDevuelveLaPoliticaSegunElTipo() {
        FabricaPoliticaCancelacion fabrica = FabricaPoliticaCancelacion.porDefecto();

        assertInstanceOf(PoliticaFlexible.class, fabrica.crear(TipoPoliticaCancelacion.FLEXIBLE));
        assertInstanceOf(PoliticaEstricta.class, fabrica.crear(TipoPoliticaCancelacion.ESTRICTA));
    }
}
