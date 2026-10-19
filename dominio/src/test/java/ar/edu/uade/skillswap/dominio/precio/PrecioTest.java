package ar.edu.uade.skillswap.dominio.precio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.Fixtures;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

class PrecioTest {

    private static final Instant DESDE = Instant.parse("2026-10-01T00:00:00Z");
    private static final Instant HASTA = Instant.parse("2026-12-01T00:00:00Z");

    @Test
    void elPrecioBaseDevuelveSuMonto() {
        assertEquals(SkillCoins.de(100), new PrecioBase(SkillCoins.de(100)).calcular());
    }

    @Test
    void losDescuentosSeAcumulanEnElOrdenDeAlta() {
        Precio precio = new PrecioBase(SkillCoins.de(200));
        precio = new DescuentoPorcentual(precio, 50);
        precio = new DescuentoFijo(precio, SkillCoins.de(30));

        assertEquals(SkillCoins.de(70), precio.calcular());
    }

    @Test
    void elPrecioNuncaEsNegativo() {
        Precio precio = new PrecioBase(SkillCoins.de(100));
        precio = new DescuentoPorcentual(precio, 60);
        precio = new DescuentoFijo(precio, SkillCoins.de(50));

        assertEquals(SkillCoins.CERO, precio.calcular());
    }

    @Test
    void laPromocionPorcentualExigeUnPorcentajeValido() {
        assertThrows(ValorInvalido.class, () -> new PromocionPorcentual(0, DESDE, HASTA));
        assertThrows(ValorInvalido.class, () -> new PromocionPorcentual(101, DESDE, HASTA));
    }

    @Test
    void laPromocionEstaVigenteSoloDentroDeSuPeriodo() {
        Promocion promocion = new PromocionPorcentual(10, DESDE, HASTA);

        assertTrue(promocion.estaVigente(Fixtures.AHORA));
        assertFalse(promocion.estaVigente(Instant.parse("2027-01-01T00:00:00Z")));
        assertFalse(promocion.estaVigente(Instant.parse("2026-09-30T00:00:00Z")));
    }

    @Test
    void laPublicacionAplicaSoloLasPromocionesVigentesYNuncaPrecioNegativo() {
        Publicacion publicacion = Fixtures.borrador();
        publicacion.agregarPromocion(new PromocionPorcentual(60, DESDE, HASTA), Fixtures.ctx(Fixtures.PROVEEDOR));
        publicacion.agregarPromocion(new PromocionMontoFijo(SkillCoins.de(70), DESDE, HASTA),
                Fixtures.ctx(Fixtures.PROVEEDOR));
        publicacion.agregarPromocion(
                new PromocionMontoFijo(SkillCoins.de(10), DESDE, Instant.parse("2026-10-15T00:00:00Z")),
                Fixtures.ctx(Fixtures.PROVEEDOR));

        assertEquals(SkillCoins.CERO, publicacion.calcularPrecio(Fixtures.AHORA));
        assertEquals(SkillCoins.de(100), publicacion.calcularPrecio(Instant.parse("2027-02-01T00:00:00Z")));
    }

    @Test
    void laPublicacionSinPromocionesCobraElPrecioBase() {
        assertEquals(SkillCoins.de(100), Fixtures.borrador().calcularPrecio(Fixtures.AHORA));
    }
}
