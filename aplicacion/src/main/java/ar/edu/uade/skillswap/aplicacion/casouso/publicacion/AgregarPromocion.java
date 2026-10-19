package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class AgregarPromocion {

    private final EjecutorTransaccional transaccion;
    private final PublicacionRepositorio publicaciones;
    private final Clock reloj;

    public AgregarPromocion(EjecutorTransaccional transaccion, PublicacionRepositorio publicaciones, Clock reloj) {
        this.transaccion = transaccion;
        this.publicaciones = publicaciones;
        this.reloj = reloj;
    }

    public PublicacionVista ejecutar(AgregarPromocionComando comando) {
        return transaccion.ejecutar(() -> {
            Publicacion publicacion = publicaciones.obtenerPorId(comando.publicacionId());
            publicacion.agregarPromocion(
                    comando.tipo().crear(comando.valor(), comando.vigenteDesde(), comando.vigenteHasta()),
                    ContextoAccion.de(comando.actor(), reloj.instant()));
            return ResultadoTransaccion.sinEventos(PublicacionVista.de(publicaciones.guardar(publicacion)));
        });
    }
}
