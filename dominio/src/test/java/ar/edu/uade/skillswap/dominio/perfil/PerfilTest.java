package ar.edu.uade.skillswap.dominio.perfil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.Fixtures;
import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;

class PerfilTest {

    @Test
    void laPersonaPublicaMentoriasYServiciosPeroNoDesafios() {
        Perfil persona = Fixtures.perfilProveedor();

        assertTrue(persona.puedePublicar(TipoPublicacion.MENTORIA));
        assertTrue(persona.puedePublicar(TipoPublicacion.SERVICIO));
        assertFalse(persona.puedePublicar(TipoPublicacion.DESAFIO));
    }

    @Test
    void laOrganizacionSoloPublicaDesafios() {
        Perfil organizacion = Fixtures.perfilOrganizacion();

        assertTrue(organizacion.puedePublicar(TipoPublicacion.DESAFIO));
        assertFalse(organizacion.puedePublicar(TipoPublicacion.MENTORIA));
    }

    @Test
    void elNombreVisibleYLaZonaHorariaSonObligatorios() {
        assertThrows(ValorInvalido.class,
                () -> Perfil.crear(Fixtures.CLIENTE, TipoPerfil.PERSONA, " ", "a@b.com", ZoneId.of("UTC")));
        assertThrows(ValorInvalido.class,
                () -> Perfil.crear(Fixtures.CLIENTE, TipoPerfil.PERSONA, "Nombre", "a@b.com", null));
    }

    @Test
    void nacePorDefectoActivoConCanalInternoYSinReputacion() {
        Perfil perfil = Fixtures.perfilCliente();

        assertTrue(perfil.estaActivo());
        assertEquals(CanalPreferido.INTERNA, perfil.getCanalPreferido());
        assertEquals(0, perfil.getReputacion().cantidad());
    }

    @Test
    void permiteActualizarSusDatos() {
        Perfil perfil = Fixtures.perfilCliente();

        perfil.actualizarDatos("Nuevo nombre", "Biografía", ZoneId.of("America/Argentina/Buenos_Aires"));
        perfil.definirHabilidades(Set.of(Fixtures.JAVA));
        perfil.definirIntereses(Set.of(Fixtures.SQL));
        perfil.definirDisponibilidad(List.of(Fixtures.FRANJA));
        perfil.elegirCanal(CanalPreferido.EMAIL);

        assertEquals("Nuevo nombre", perfil.getNombreVisible());
        assertEquals("Biografía", perfil.getBiografia());
        assertEquals(Set.of(Fixtures.JAVA, Fixtures.SQL), perfil.habilidadesEInteresesCombinados());
        assertEquals(1, perfil.getDisponibilidad().size());
        assertEquals(CanalPreferido.EMAIL, perfil.getCanalPreferido());
    }

    @Test
    void laReputacionSoloCambiaAlRecibirCalificaciones() {
        Perfil perfil = Fixtures.perfilProveedor();

        perfil.recibirCalificacion(new Puntaje(5));
        perfil.recibirCalificacion(new Puntaje(3));

        assertEquals(4.0, perfil.getReputacion().promedio());
        assertEquals(2, perfil.getReputacion().cantidad());
    }

    @Test
    void laBajaEsLogicaYBloqueaLasModificaciones() {
        Perfil perfil = Fixtures.perfilCliente();

        perfil.darDeBaja(Fixtures.AHORA);

        assertFalse(perfil.estaActivo());
        assertEquals(Fixtures.AHORA, perfil.getFechaBaja());
        assertFalse(perfil.puedePublicar(TipoPublicacion.MENTORIA));
        assertThrows(TransicionInvalida.class, () -> perfil.darDeBaja(Fixtures.AHORA));
        assertThrows(TransicionInvalida.class, () -> perfil.elegirCanal(CanalPreferido.PUSH));
    }
}
