package ar.edu.uade.skillswap.infraestructura.externo;

import ar.edu.uade.skillswap.aplicacion.puerto.ServicioIdentidad;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public class AdaptadorIdentidadSimulada implements ServicioIdentidad {

    private final ServicioIdentidadSimulado servicio;

    public AdaptadorIdentidadSimulada(ServicioIdentidadSimulado servicio) {
        this.servicio = servicio;
    }

    @Override
    public boolean existeUsuario(UsuarioId usuario) {
        return servicio.existe(usuario.valor());
    }
}
