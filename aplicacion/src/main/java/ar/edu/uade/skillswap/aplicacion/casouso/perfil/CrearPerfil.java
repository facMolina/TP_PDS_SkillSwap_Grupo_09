package ar.edu.uade.skillswap.aplicacion.casouso.perfil;

import java.time.Clock;
import java.time.Instant;

import ar.edu.uade.skillswap.aplicacion.comun.ConfiguracionAplicacion;
import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.ResultadoTransaccion;
import ar.edu.uade.skillswap.aplicacion.error.PerfilYaExiste;
import ar.edu.uade.skillswap.aplicacion.error.UsuarioInexistente;
import ar.edu.uade.skillswap.aplicacion.puerto.BilleteraRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.ServicioIdentidad;
import ar.edu.uade.skillswap.dominio.billetera.Billetera;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class CrearPerfil {

    private final EjecutorTransaccional transaccion;
    private final PerfilRepositorio perfiles;
    private final BilleteraRepositorio billeteras;
    private final ServicioIdentidad identidad;
    private final ConfiguracionAplicacion configuracion;
    private final Clock reloj;

    public CrearPerfil(EjecutorTransaccional transaccion, PerfilRepositorio perfiles, BilleteraRepositorio billeteras,
                       ServicioIdentidad identidad, ConfiguracionAplicacion configuracion, Clock reloj) {
        this.transaccion = transaccion;
        this.perfiles = perfiles;
        this.billeteras = billeteras;
        this.identidad = identidad;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    public PerfilVista ejecutar(CrearPerfilComando comando) {
        return transaccion.ejecutar(() -> {
            if (!identidad.existeUsuario(comando.usuarioId())) {
                throw new UsuarioInexistente("El servicio de identidad no reconoce al usuario");
            }
            if (perfiles.existePorUsuario(comando.usuarioId())) {
                throw new PerfilYaExiste("El usuario ya tiene un perfil");
            }
            Instant ahora = reloj.instant();
            Perfil perfil = perfiles.guardar(Perfil.crear(comando.usuarioId(), comando.tipo(),
                    comando.nombreVisible(), comando.email(), comando.zonaHoraria()));
            billeteras.guardar(Billetera.abrir(comando.usuarioId(), configuracion.saldoInicial(), ahora));
            return ResultadoTransaccion.sinEventos(PerfilVista.de(perfil));
        });
    }
}
