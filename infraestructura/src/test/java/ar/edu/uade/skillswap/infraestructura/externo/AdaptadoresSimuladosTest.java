package ar.edu.uade.skillswap.infraestructura.externo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

class AdaptadoresSimuladosTest {

    @Test
    void elAdaptadorDeEmailTraduceElPuertoAlClienteSimulado() {
        ClienteCorreoSimulado cliente = new ClienteCorreoSimulado();

        new AdaptadorEmailSimulado(cliente).enviarCorreo("ana@mail.com", "Asunto", "Cuerpo");

        assertEquals(List.of(new ClienteCorreoSimulado.CorreoEnviado("ana@mail.com", "Asunto", "Cuerpo", false)),
                cliente.enviados());
    }

    @Test
    void elAdaptadorPushDirigeElMensajeAlDispositivoDelUsuario() {
        ServicioPushSimulado servicio = new ServicioPushSimulado();

        new AdaptadorPushSimulado(servicio).enviarPush(new UsuarioId("ana"), "Titulo", "Texto");

        assertEquals(List.of(new ServicioPushSimulado.PushEnviado("dispositivo-ana", "Titulo", "Texto")),
                servicio.enviados());
    }

    @Test
    void elAdaptadorDeIdentidadConsultaLasCuentasRegistradas() {
        ServicioIdentidadSimulado servicio = new ServicioIdentidadSimulado(List.of("ana", "beto"));
        AdaptadorIdentidadSimulada adaptador = new AdaptadorIdentidadSimulada(servicio);

        assertTrue(adaptador.existeUsuario(new UsuarioId("ana")));
        assertFalse(adaptador.existeUsuario(new UsuarioId("carla")));
    }

    @Test
    void elInicioDeSesionSimuladoFijaElUsuarioActivo() {
        ServicioIdentidadSimulado servicio = new ServicioIdentidadSimulado(List.of("ana"));
        assertNull(servicio.usuarioActivo());

        servicio.iniciarSesion("ana");

        assertEquals("ana", servicio.usuarioActivo());
        assertThrows(IllegalArgumentException.class, () -> servicio.iniciarSesion("carla"));
    }
}
