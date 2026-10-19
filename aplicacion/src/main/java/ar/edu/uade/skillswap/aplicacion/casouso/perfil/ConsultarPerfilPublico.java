package ar.edu.uade.skillswap.aplicacion.casouso.perfil;

import ar.edu.uade.skillswap.aplicacion.comun.ConfiguracionAplicacion;
import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.aplicacion.puerto.ConsultaPublicaciones;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class ConsultarPerfilPublico {

    private final EjecutorTransaccional transaccion;
    private final PerfilRepositorio perfiles;
    private final ConsultaPublicaciones publicaciones;
    private final ContratacionRepositorio contrataciones;
    private final ConfiguracionAplicacion configuracion;

    public ConsultarPerfilPublico(EjecutorTransaccional transaccion, PerfilRepositorio perfiles,
                                  ConsultaPublicaciones publicaciones, ContratacionRepositorio contrataciones,
                                  ConfiguracionAplicacion configuracion) {
        this.transaccion = transaccion;
        this.perfiles = perfiles;
        this.publicaciones = publicaciones;
        this.contrataciones = contrataciones;
        this.configuracion = configuracion;
    }

    public PerfilPublicoVista ejecutar(UsuarioId usuario) {
        return transaccion.consultar(() -> {
            Perfil perfil = perfiles.obtenerPorUsuario(usuario);
            if (!perfil.estaActivo()) {
                throw new RecursoNoEncontrado("No existe el perfil " + usuario.valor());
            }
            return new PerfilPublicoVista(
                    PerfilVista.de(perfil),
                    publicaciones.publicacionesVigentesDe(usuario),
                    contrataciones.historialResumido(usuario, configuracion.maximoHistorialResumido()).stream()
                            .map(ResumenContratacion::de)
                            .toList());
        });
    }
}
