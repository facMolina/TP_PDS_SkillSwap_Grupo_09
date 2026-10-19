package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class EditarBorrador {

    private final EjecutorTransaccional transaccion;
    private final PublicacionRepositorio publicaciones;
    private final ResolvedorDatosPublicacion resolvedor;
    private final Clock reloj;

    public EditarBorrador(EjecutorTransaccional transaccion, PublicacionRepositorio publicaciones,
                          ResolvedorDatosPublicacion resolvedor, Clock reloj) {
        this.transaccion = transaccion;
        this.publicaciones = publicaciones;
        this.resolvedor = resolvedor;
        this.reloj = reloj;
    }

    public PublicacionVista ejecutar(EditarBorradorComando comando) {
        return transaccion.ejecutar(() -> {
            Publicacion publicacion = publicaciones.obtenerPorId(comando.publicacionId());
            publicacion.editar(resolvedor.resolver(comando.datos()),
                    ContextoAccion.de(comando.actor(), reloj.instant()));
            return ResultadoTransaccion.sinEventos(PublicacionVista.de(publicaciones.guardar(publicacion)));
        });
    }
}
