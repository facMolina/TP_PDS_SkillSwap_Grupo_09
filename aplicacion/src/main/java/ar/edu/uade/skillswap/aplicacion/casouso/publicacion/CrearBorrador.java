package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.time.Clock;
import java.time.Instant;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class CrearBorrador {

    private final EjecutorTransaccional transaccion;
    private final PerfilRepositorio perfiles;
    private final PublicacionRepositorio publicaciones;
    private final ResolvedorDatosPublicacion resolvedor;
    private final Clock reloj;

    public CrearBorrador(EjecutorTransaccional transaccion, PerfilRepositorio perfiles,
                         PublicacionRepositorio publicaciones, ResolvedorDatosPublicacion resolvedor, Clock reloj) {
        this.transaccion = transaccion;
        this.perfiles = perfiles;
        this.publicaciones = publicaciones;
        this.resolvedor = resolvedor;
        this.reloj = reloj;
    }

    public PublicacionVista ejecutar(CrearBorradorComando comando) {
        return transaccion.ejecutar(() -> {
            Instant ahora = reloj.instant();
            Perfil autor = perfiles.obtenerPorUsuario(comando.actor());
            Publicacion publicacion = Publicacion.crearBorrador(autor, resolvedor.resolver(comando.datos()), ahora);
            ContextoAccion ctx = ContextoAccion.de(comando.actor(), ahora);
            if (comando.franjas() != null) {
                for (FranjaHoraria franja : comando.franjas()) {
                    publicacion.agregarFranja(franja, ctx);
                }
            }
            return ResultadoTransaccion.sinEventos(PublicacionVista.de(publicaciones.guardar(publicacion)));
        });
    }
}
