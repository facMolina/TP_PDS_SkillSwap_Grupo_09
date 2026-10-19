package ar.edu.uade.skillswap.aplicacion.casouso.perfil;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.RechazadorDeSolicitudes;
import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.error.OperacionNoPermitida;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class DarDeBajaPerfil {

    private static final String MOTIVO = "Baja del perfil del autor";

    private final EjecutorTransaccional transaccion;
    private final PerfilRepositorio perfiles;
    private final PublicacionRepositorio publicaciones;
    private final ContratacionRepositorio contrataciones;
    private final RechazadorDeSolicitudes rechazador;
    private final Clock reloj;

    public DarDeBajaPerfil(EjecutorTransaccional transaccion, PerfilRepositorio perfiles,
                           PublicacionRepositorio publicaciones, ContratacionRepositorio contrataciones,
                           RechazadorDeSolicitudes rechazador, Clock reloj) {
        this.transaccion = transaccion;
        this.perfiles = perfiles;
        this.publicaciones = publicaciones;
        this.contrataciones = contrataciones;
        this.rechazador = rechazador;
        this.reloj = reloj;
    }

    public void ejecutar(DarDeBajaComando comando) {
        transaccion.ejecutar(() -> {
            Perfil perfil = perfiles.obtenerPorUsuario(comando.actor());
            if (contrataciones.existenComprometidasDe(comando.actor())) {
                throw new OperacionNoPermitida("El perfil tiene contrataciones comprometidas");
            }
            Instant ahora = reloj.instant();
            ContextoAccion ctx = ContextoAccion.de(comando.actor(), ahora);
            List<EventoContratacion> eventos = new ArrayList<>();
            for (Publicacion publicacion : publicaciones.buscarVigentesPorAutor(comando.actor())) {
                publicacion.cancelar(ctx, MOTIVO);
                publicaciones.guardar(publicacion);
                eventos.addAll(rechazador.rechazar(publicacion.getId(), ahora, MOTIVO));
            }
            perfil.darDeBaja(ahora);
            perfiles.guardar(perfil);
            return ResultadoTransaccion.de(null, eventos);
        });
    }
}
