package ar.edu.uade.skillswap.aplicacion.casouso.perfil;

import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.puerto.CatalogoRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class ActualizarPerfil {

    private final EjecutorTransaccional transaccion;
    private final PerfilRepositorio perfiles;
    private final CatalogoRepositorio catalogo;

    public ActualizarPerfil(EjecutorTransaccional transaccion, PerfilRepositorio perfiles,
                            CatalogoRepositorio catalogo) {
        this.transaccion = transaccion;
        this.perfiles = perfiles;
        this.catalogo = catalogo;
    }

    public PerfilVista ejecutar(ActualizarPerfilComando comando) {
        return transaccion.ejecutar(() -> {
            Perfil perfil = perfiles.obtenerPorUsuario(comando.actor());
            perfil.actualizarDatos(comando.nombreVisible(), comando.biografia(), comando.zonaHoraria());
            if (comando.habilidadIds() != null) {
                perfil.definirHabilidades(catalogo.buscarHabilidades(comando.habilidadIds()));
            }
            if (comando.interesIds() != null) {
                perfil.definirIntereses(catalogo.buscarHabilidades(comando.interesIds()));
            }
            if (comando.disponibilidad() != null) {
                perfil.definirDisponibilidad(comando.disponibilidad());
            }
            if (comando.canal() != null) {
                perfil.elegirCanal(comando.canal());
            }
            return ResultadoTransaccion.sinEventos(PerfilVista.de(perfiles.guardar(perfil)));
        });
    }
}
