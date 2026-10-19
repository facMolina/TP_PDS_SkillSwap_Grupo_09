package ar.edu.uade.skillswap.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.casouso.billetera.ConsultaBilletera;
import ar.edu.uade.skillswap.aplicacion.casouso.calificacion.CalificarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.CancelarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.notificacion.ListadoNotificaciones;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CambiarEstadoComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CrearBorradorComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.PublicacionVista;
import ar.edu.uade.skillswap.dominio.billetera.TipoMovimiento;
import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.CalificacionNoPermitida;
import ar.edu.uade.skillswap.dominio.error.PublicacionIncompleta;
import ar.edu.uade.skillswap.dominio.error.SaldoInsuficiente;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.infraestructura.soporte.EntornoPrueba;
import ar.edu.uade.skillswap.infraestructura.soporte.PruebaConBaseDatos;

class FlujoPrincipalPersistenteTest extends PruebaConBaseDatos {

    @Test
    void elRecorridoPrincipalSePersisteDePunteAPunta() {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(3);

        ContratacionVista solicitada = solicitar(CLIENTE, publicacion, F1);
        assertEquals("PENDIENTE", solicitada.estado());
        assertEquals("ACEPTADA", aceptar(solicitada.id()).estado());
        assertEquals("CONFIRMADA", pagar(CLIENTE, solicitada.id()).estado());
        assertEquals("EN_CURSO",
                app.aplicacion.iniciar.ejecutar(new ContratacionComando(PROVEEDOR, solicitada.id())).estado());
        assertEquals("COMPLETADA",
                app.aplicacion.completar.ejecutar(new ContratacionComando(PROVEEDOR, solicitada.id())).estado());
        app.aplicacion.calificar.ejecutar(new CalificarComando(CLIENTE, solicitada.id(), new Puntaje(5), "Excelente"));

        assertEquals(SkillCoins.de(900), saldo(CLIENTE));
        assertEquals(SkillCoins.de(1100), saldo(PROVEEDOR));
        var movimientos = app.aplicacion.consultarBilletera.ejecutar(new ConsultaBilletera(CLIENTE, null)).movimientos();
        assertEquals(List.of(TipoMovimiento.SALDO_INICIAL, TipoMovimiento.PAGO),
                movimientos.elementos().stream().map(m -> m.tipo()).toList());
        assertEquals(5.0, app.aplicacion.consultarPerfilPublico.ejecutar(PROVEEDOR).perfil().reputacion());
        assertEquals(1, app.aplicacion.consultarPerfilPublico.ejecutar(PROVEEDOR).historial().size());
    }

    @Test
    void lasNotificacionesInternasSePersistenParaCadaDestinatario() {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(3);
        ContratacionVista solicitada = solicitar(CLIENTE, publicacion, F1);
        aceptar(solicitada.id());

        var delProveedor = app.aplicacion.listarNotificaciones.ejecutar(new ListadoNotificaciones(PROVEEDOR, null));
        var delCliente = app.aplicacion.listarNotificaciones.ejecutar(new ListadoNotificaciones(CLIENTE, null));

        assertEquals(1, delProveedor.total());
        assertEquals(1, delCliente.total());
        assertFalse(delCliente.elementos().get(0).leida());
    }

    @Test
    void unaOfertaIncompletaSeRechazaYNoQuedaPublicada() {
        registrarProveedorYCliente();
        PublicacionVista borrador = app.aplicacion.crearBorrador.ejecutar(new CrearBorradorComando(PROVEEDOR,
                datos("Sin franjas", 1, 50, Set.of(JAVA)), List.of()));

        assertThrows(PublicacionIncompleta.class, () -> app.aplicacion.cambiarEstado.ejecutar(
                new CambiarEstadoComando(PROVEEDOR, borrador.id(), AccionPublicacion.PUBLICAR, null)));
    }

    @Test
    void noSePuedePagarDosVecesNiConSaldoInsuficiente() {
        registrarProveedorYCliente();
        Long cara = publicar(PROVEEDOR, datos("Mentoría cara", 1, 5000, Set.of(JAVA)), List.of(F1));
        Long barata = publicacionBasica(3);
        ContratacionVista enCara = solicitar(CLIENTE, cara, F1);
        aceptar(enCara.id());
        ContratacionVista enBarata = solicitar(CLIENTE, barata, F2);
        aceptar(enBarata.id());

        assertThrows(SaldoInsuficiente.class, () -> pagar(CLIENTE, enCara.id()));
        assertEquals(SkillCoins.de(1000), saldo(CLIENTE));

        pagar(CLIENTE, enBarata.id());
        assertThrows(TransicionInvalida.class, () -> pagar(CLIENTE, enBarata.id()));
        assertEquals(SkillCoins.de(900), saldo(CLIENTE));
    }

    @Test
    void soloUnParticipanteDeUnaContratacionCompletadaPuedeCalificar() {
        registrarProveedorYCliente();
        registrar(OTRO, TipoPerfil.PERSONA);
        Long publicacion = publicacionBasica(3);
        ContratacionVista contratacion = solicitar(CLIENTE, publicacion, F1);
        aceptar(contratacion.id());
        pagar(CLIENTE, contratacion.id());
        app.aplicacion.iniciar.ejecutar(new ContratacionComando(PROVEEDOR, contratacion.id()));
        app.aplicacion.completar.ejecutar(new ContratacionComando(PROVEEDOR, contratacion.id()));

        assertThrows(CalificacionNoPermitida.class, () -> app.aplicacion.calificar.ejecutar(
                new CalificarComando(OTRO, contratacion.id(), new Puntaje(1), "Ajeno")));
    }

    @Test
    void lasContratacionesAceptadasSinPagoVencenYLiberanElCupo() {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(1);
        ContratacionVista contratacion = solicitar(CLIENTE, publicacion, F1);
        aceptar(contratacion.id());

        EntornoPrueba.RELOJ.avanzar(Duration.ofHours(49));
        int vencidas = app.aplicacion.vencer.ejecutar();

        assertEquals(1, vencidas);
        assertEquals("CANCELADA", estadoDe(contratacion.id()));
        ContratacionVista nueva = solicitar(CLIENTE, publicacion, F1);
        assertEquals("PENDIENTE", nueva.estado());
        assertEquals("ACEPTADA", aceptar(nueva.id()).estado());
    }

    @Test
    void cancelarUnaContratacionConfirmadaReintegraSegunLaPolitica() {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(3);
        ContratacionVista contratacion = solicitar(CLIENTE, publicacion, F1);
        aceptar(contratacion.id());
        pagar(CLIENTE, contratacion.id());

        app.aplicacion.cancelar.ejecutar(new CancelarComando(CLIENTE, contratacion.id(), "Imprevisto"));

        assertEquals(SkillCoins.de(1000), saldo(CLIENTE));
        assertEquals(SkillCoins.de(1000), saldo(PROVEEDOR));
    }

    private String estadoDe(Long contratacion) {
        return app.aplicacion.transaccion.consultar(
                () -> app.puertos.contrataciones().obtenerPorId(contratacion).getNombreEstado());
    }

    private SkillCoins saldo(UsuarioId usuario) {
        return app.aplicacion.consultarBilletera.ejecutar(new ConsultaBilletera(usuario, null)).saldo();
    }
}
