package ar.edu.uade.skillswap.dominio.comun;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

class ValoresTest {

    private static FranjaHoraria franja(String inicio, String fin) {
        return new FranjaHoraria(Instant.parse(inicio), Instant.parse(fin));
    }

    @Test
    void skillCoinsNoAceptaMontosNegativos() {
        assertThrows(ValorInvalido.class, () -> SkillCoins.de(-1));
    }

    @Test
    void skillCoinsSumaYRestaMontosEnteros() {
        assertEquals(SkillCoins.de(150), SkillCoins.de(100).sumar(SkillCoins.de(50)));
        assertEquals(SkillCoins.de(40), SkillCoins.de(100).restar(SkillCoins.de(60)));
    }

    @Test
    void skillCoinsNoPuedeRestarMasDeLoQueTiene() {
        assertThrows(ValorInvalido.class, () -> SkillCoins.de(10).restar(SkillCoins.de(11)));
        assertEquals(SkillCoins.CERO, SkillCoins.de(10).restarHastaCero(SkillCoins.de(11)));
    }

    @Test
    void skillCoinsCalculaPorcentajesRedondeandoHaciaAbajo() {
        assertEquals(SkillCoins.de(33), SkillCoins.de(99).porcentaje(34));
        assertEquals(SkillCoins.de(1), SkillCoins.de(3).porcentaje(50));
        assertThrows(ValorInvalido.class, () -> SkillCoins.de(10).porcentaje(101));
        assertThrows(ValorInvalido.class, () -> SkillCoins.de(10).porcentaje(-1));
    }

    @Test
    void skillCoinsCompara() {
        assertTrue(SkillCoins.de(1).esMenorQue(SkillCoins.de(2)));
        assertFalse(SkillCoins.de(2).esMenorQue(SkillCoins.de(2)));
        assertTrue(SkillCoins.CERO.esCero());
    }

    @Test
    void usuarioIdExigeValor() {
        assertThrows(ValorInvalido.class, () -> new UsuarioId(" "));
        assertThrows(ValorInvalido.class, () -> new UsuarioId(null));
    }

    @Test
    void puntajeAceptaSoloDeUnoACinco() {
        assertEquals(5, new Puntaje(5).valor());
        assertThrows(ValorInvalido.class, () -> new Puntaje(0));
        assertThrows(ValorInvalido.class, () -> new Puntaje(6));
    }

    @Test
    void franjaExigeInicioAnteriorAlFin() {
        assertThrows(ValorInvalido.class, () -> franja("2026-11-10T16:00:00Z", "2026-11-10T15:00:00Z"));
        assertThrows(ValorInvalido.class, () -> new FranjaHoraria(null, Instant.now()));
    }

    @Test
    void franjaDetectaSolapamientoYContencion() {
        FranjaHoraria base = franja("2026-11-10T15:00:00Z", "2026-11-10T17:00:00Z");

        assertTrue(base.seSolapaCon(franja("2026-11-10T16:00:00Z", "2026-11-10T18:00:00Z")));
        assertFalse(base.seSolapaCon(franja("2026-11-10T17:00:00Z", "2026-11-10T18:00:00Z")));
        assertTrue(base.contiene(franja("2026-11-10T15:30:00Z", "2026-11-10T16:30:00Z")));
        assertFalse(base.contiene(franja("2026-11-10T14:00:00Z", "2026-11-10T16:00:00Z")));
        assertEquals(Duration.ofHours(2), base.duracion());
    }

    @Test
    void reputacionCalculaPromedioConDosDecimales() {
        Reputacion reputacion = Reputacion.SIN_CALIFICACIONES;
        assertEquals(0, reputacion.promedio());

        reputacion = reputacion.incorporar(new Puntaje(5)).incorporar(new Puntaje(4)).incorporar(new Puntaje(4));

        assertEquals(3, reputacion.cantidad());
        assertEquals(4.33, reputacion.promedio());
    }

    @Test
    void contextoDelSistemaUsaElUsuarioSistema() {
        Instant ahora = Instant.parse("2026-11-01T12:00:00Z");

        assertEquals(UsuarioId.SISTEMA, ContextoAccion.delSistema(ahora).actor());
        assertEquals(ahora, ContextoAccion.delSistema(ahora).ahora());
    }

    @Test
    void historialNoPermiteModificarseDesdeAfuera() {
        HistorialTransiciones historial = new HistorialTransiciones();
        historial.registrar(new TransicionEstado(Instant.now(), UsuarioId.SISTEMA, "A", "B", "motivo"));

        assertEquals(1, historial.getTransiciones().size());
        assertThrows(UnsupportedOperationException.class, () -> historial.getTransiciones().clear());
    }
}
