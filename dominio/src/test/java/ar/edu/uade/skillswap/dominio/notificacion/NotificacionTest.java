package ar.edu.uade.skillswap.dominio.notificacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.Fixtures;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;

class NotificacionTest {

    private final Notificacion notificacion =
            new Notificacion(Fixtures.CLIENTE, TipoEvento.PAGO_CONFIRMADO, "Pago confirmado", Fixtures.AHORA);

    @Test
    void nacePendienteDeLectura() {
        assertFalse(notificacion.estaLeida());
    }

    @Test
    void soloSuDestinatarioPuedeMarcarlaComoLeida() {
        notificacion.marcarLeida(Fixtures.CLIENTE);

        assertTrue(notificacion.estaLeida());
    }

    @Test
    void otroUsuarioNoPuedeMarcarla() {
        assertThrows(PermisoDenegado.class, () -> notificacion.marcarLeida(Fixtures.PROVEEDOR));
        assertFalse(notificacion.estaLeida());
    }
}
