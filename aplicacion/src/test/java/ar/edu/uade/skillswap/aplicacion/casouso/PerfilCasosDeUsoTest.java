package ar.edu.uade.skillswap.aplicacion.casouso;

import static ar.edu.uade.skillswap.aplicacion.Escenario.CLIENTE;
import static ar.edu.uade.skillswap.aplicacion.Escenario.F1;
import static ar.edu.uade.skillswap.aplicacion.Escenario.PROVEEDOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.Escenario;
import ar.edu.uade.skillswap.aplicacion.busqueda.PublicacionResumen;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.ActualizarPerfilComando;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.CrearPerfil;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.CrearPerfilComando;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.DarDeBajaComando;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.PerfilPublicoVista;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.PerfilVista;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.error.OperacionNoPermitida;
import ar.edu.uade.skillswap.aplicacion.error.PerfilYaExiste;
import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.aplicacion.error.UsuarioInexistente;
import ar.edu.uade.skillswap.dominio.billetera.TipoMovimiento;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.perfil.CanalPreferido;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;

class PerfilCasosDeUsoTest {

    private Escenario e;

    @BeforeEach
    void preparar() {
        e = new Escenario();
    }

    private CrearPerfilComando comandoCrear(UsuarioId usuario) {
        return new CrearPerfilComando(usuario, TipoPerfil.PERSONA, "Nombre", "mail@mail.com", ZoneId.of("UTC"));
    }

    @Test
    void crearPerfilAbreLaBilleteraConElSaldoInicial() {
        PerfilVista vista = e.crearPerfil.ejecutar(comandoCrear(CLIENTE));

        assertEquals(CLIENTE, vista.usuarioId());
        assertTrue(vista.activo());
        assertEquals(0, vista.cantidadCalificaciones());
        assertEquals(SkillCoins.de(1000), e.saldoDe(CLIENTE));
        assertEquals(TipoMovimiento.SALDO_INICIAL, e.billeteras.movimientosDe(CLIENTE).get(0).getTipo());
    }

    @Test
    void crearPerfilExigeUnUsuarioConocidoPorElServicioDeIdentidad() {
        CrearPerfil sinIdentidad = new CrearPerfil(
                e.transaccion, e.perfiles, e.billeteras, usuario -> false, e.configuracion, e.reloj);

        assertThrows(UsuarioInexistente.class, () -> sinIdentidad.ejecutar(comandoCrear(CLIENTE)));
        assertFalse(e.perfiles.existePorUsuario(CLIENTE));
    }

    @Test
    void unUsuarioNoPuedeTenerDosPerfiles() {
        e.crearPerfil.ejecutar(comandoCrear(CLIENTE));

        assertThrows(PerfilYaExiste.class, () -> e.crearPerfil.ejecutar(comandoCrear(CLIENTE)));
    }

    @Test
    void actualizarPerfilResuelveHabilidadesEInteresesDelCatalogo() {
        e.crearPerfil.ejecutar(comandoCrear(CLIENTE));

        PerfilVista vista = e.actualizarPerfil.ejecutar(new ActualizarPerfilComando(CLIENTE, "Nuevo", "Bio",
                ZoneId.of("America/Argentina/Buenos_Aires"), Set.of(10L), Set.of(11L), List.of(F1),
                CanalPreferido.PUSH));

        assertEquals("Nuevo", vista.nombreVisible());
        assertEquals(Set.of(Escenario.JAVA), vista.habilidades());
        assertEquals(Set.of(Escenario.SQL), vista.intereses());
        assertEquals(CanalPreferido.PUSH, vista.canalPreferido());
        assertEquals(1, e.perfiles.obtenerPorUsuario(CLIENTE).getDisponibilidad().size());
    }

    @Test
    void actualizarPerfilSinListasConservaLoAnterior() {
        e.crearPerfil.ejecutar(comandoCrear(CLIENTE));
        e.actualizarPerfil.ejecutar(new ActualizarPerfilComando(CLIENTE, "Uno", null, ZoneId.of("UTC"),
                Set.of(10L), null, null, CanalPreferido.EMAIL));

        PerfilVista vista = e.actualizarPerfil.ejecutar(
                new ActualizarPerfilComando(CLIENTE, "Dos", null, ZoneId.of("UTC"), null, null, null, null));

        assertEquals("Dos", vista.nombreVisible());
        assertEquals(Set.of(Escenario.JAVA), vista.habilidades());
        assertEquals(CanalPreferido.EMAIL, vista.canalPreferido());
    }

    @Test
    void actualizarUnPerfilInexistenteFalla() {
        assertThrows(RecursoNoEncontrado.class, () -> e.actualizarPerfil.ejecutar(
                new ActualizarPerfilComando(CLIENTE, "x", null, ZoneId.of("UTC"), null, null, null, null)));
    }

    @Test
    void elPerfilPublicoIncluyePublicacionesVigentesEHistorialResumido() {
        e.conPerfiles();
        Long publicacion = e.publicacionPublicada();
        PublicacionResumen resumen = new PublicacionResumen(publicacion, "Mentoría", "Nombre proveedor", SkillCoins.de(100), 0);
        when(e.consulta.publicacionesVigentesDe(PROVEEDOR)).thenReturn(List.of(resumen));
        ContratacionVista completada = e.completada(publicacion);

        PerfilPublicoVista vista = e.consultarPerfilPublico.ejecutar(PROVEEDOR);

        assertEquals(List.of(resumen), vista.publicacionesVigentes());
        assertEquals(1, vista.historial().size());
        assertEquals(completada.id(), vista.historial().get(0).contratacionId());
        assertEquals("COMPLETADA", vista.historial().get(0).estado());
    }

    @Test
    void unPerfilDadoDeBajaDejaDeSerVisible() {
        e.conPerfiles();
        e.darDeBajaPerfil.ejecutar(new DarDeBajaComando(CLIENTE));

        assertThrows(RecursoNoEncontrado.class, () -> e.consultarPerfilPublico.ejecutar(CLIENTE));
    }

    @Test
    void laBajaCancelaLasPublicacionesVigentesYRechazaLasSolicitudesPendientes() {
        e.conPerfiles();
        Long publicacion = e.publicacionPublicada();
        ContratacionVista pendiente = e.solicitarFranja(publicacion, F1);

        e.darDeBajaPerfil.ejecutar(new DarDeBajaComando(PROVEEDOR));

        assertFalse(e.perfiles.obtenerPorUsuario(PROVEEDOR).estaActivo());
        assertEquals("CANCELADA", e.publicaciones.obtenerPorId(publicacion).getNombreEstado());
        assertEquals("RECHAZADA", e.contrataciones.obtenerPorId(pendiente.id()).getNombreEstado());
        var notificaciones = e.notificaciones.listarPorDestinatario(CLIENTE, SolicitudPagina.primera());
        assertEquals(TipoEvento.SOLICITUD_RECHAZADA, notificaciones.elementos().get(0).getTipo());
    }

    @Test
    void laBajaSeRechazaSiHayContratacionesComprometidas() {
        e.conPerfiles();
        Long publicacion = e.publicacionPublicada();
        e.confirmada(publicacion, F1);

        assertThrows(OperacionNoPermitida.class, () -> e.darDeBajaPerfil.ejecutar(new DarDeBajaComando(PROVEEDOR)));
        assertThrows(OperacionNoPermitida.class, () -> e.darDeBajaPerfil.ejecutar(new DarDeBajaComando(CLIENTE)));

        assertTrue(e.perfiles.obtenerPorUsuario(PROVEEDOR).estaActivo());
        assertEquals("PUBLICADA", e.publicaciones.obtenerPorId(publicacion).getNombreEstado());
    }

    @Test
    void laBajaDejaElHistorialIntacto() {
        e.conPerfiles();
        Long publicacion = e.publicacionPublicada();
        ContratacionVista completada = e.completada(publicacion);

        e.darDeBajaPerfil.ejecutar(new DarDeBajaComando(CLIENTE));

        assertEquals("COMPLETADA", e.contrataciones.obtenerPorId(completada.id()).getNombreEstado());
        assertFalse(e.contrataciones.obtenerPorId(completada.id()).getHistorial().getTransiciones().isEmpty());
    }
}
