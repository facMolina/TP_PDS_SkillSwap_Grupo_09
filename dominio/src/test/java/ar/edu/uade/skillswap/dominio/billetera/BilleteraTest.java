package ar.edu.uade.skillswap.dominio.billetera;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.Fixtures;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.error.SaldoInsuficiente;

class BilleteraTest {

    private final Billetera billetera = Billetera.abrir(Fixtures.CLIENTE, SkillCoins.de(100), Fixtures.AHORA);

    @Test
    void alAbrirseRegistraElSaldoInicialComoMovimiento() {
        List<MovimientoCreditos> movimientos = billetera.extraerMovimientos();

        assertEquals(SkillCoins.de(100), billetera.getSaldo());
        assertEquals(1, movimientos.size());
        assertEquals(TipoMovimiento.SALDO_INICIAL, movimientos.get(0).getTipo());
    }

    @Test
    void debitarDescuentaElSaldoYRegistraElMovimiento() {
        billetera.extraerMovimientos();

        billetera.debitar(SkillCoins.de(30), TipoMovimiento.PAGO, "contratacion 10", Fixtures.AHORA);

        MovimientoCreditos movimiento = billetera.extraerMovimientos().get(0);
        assertEquals(SkillCoins.de(70), billetera.getSaldo());
        assertEquals(SkillCoins.de(70), movimiento.getSaldoResultante());
        assertEquals(TipoMovimiento.PAGO, movimiento.getTipo());
        assertEquals("contratacion 10", movimiento.getReferencia());
    }

    @Test
    void debitarSinSaldoSuficienteNoModificaNada() {
        billetera.extraerMovimientos();

        assertThrows(SaldoInsuficiente.class,
                () -> billetera.debitar(SkillCoins.de(101), TipoMovimiento.PAGO, "x", Fixtures.AHORA));

        assertEquals(SkillCoins.de(100), billetera.getSaldo());
        assertTrue(billetera.extraerMovimientos().isEmpty());
    }

    @Test
    void acreditarSumaAlSaldo() {
        billetera.acreditar(SkillCoins.de(50), TipoMovimiento.COBRO, "x", Fixtures.AHORA);

        assertEquals(SkillCoins.de(150), billetera.getSaldo());
    }

    @Test
    void extraerMovimientosLosVaciaDespuesDeEntregarlos() {
        assertEquals(1, billetera.extraerMovimientos().size());
        assertTrue(billetera.extraerMovimientos().isEmpty());
    }

    @Test
    void sePuedeReconstruirDesdeLaPersistencia() {
        Billetera reconstruida = Billetera.reconstruir(5L, Fixtures.CLIENTE, SkillCoins.de(70), 3L);

        assertEquals(5L, reconstruida.getId());
        assertEquals(3L, reconstruida.getVersion());
        assertTrue(reconstruida.tieneSaldoPara(SkillCoins.de(70)));
        assertEquals(Fixtures.CLIENTE, reconstruida.getUsuarioId());
    }
}
