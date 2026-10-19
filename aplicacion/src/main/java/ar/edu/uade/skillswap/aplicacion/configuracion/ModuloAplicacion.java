package ar.edu.uade.skillswap.aplicacion.configuracion;

import java.time.Clock;

import ar.edu.uade.skillswap.aplicacion.casouso.billetera.ConsultarBilletera;
import ar.edu.uade.skillswap.aplicacion.casouso.busqueda.BuscarPublicaciones;
import ar.edu.uade.skillswap.aplicacion.casouso.busqueda.RecomendarPublicaciones;
import ar.edu.uade.skillswap.aplicacion.casouso.calificacion.CalificarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.AceptarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.CancelarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.CompletarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ConsultarComprobante;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.IniciarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.PagarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ProponerReprogramacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.RechazarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ResponderReprogramacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.SolicitarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.VencerContratacionesSinPago;
import ar.edu.uade.skillswap.aplicacion.casouso.notificacion.ListarNotificaciones;
import ar.edu.uade.skillswap.aplicacion.casouso.notificacion.MarcarNotificacionLeida;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.ActualizarPerfil;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.ConsultarPerfilPublico;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.CrearPerfil;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.DarDeBajaPerfil;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AgregarPromocion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CambiarEstadoPublicacion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CrearBorrador;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.EditarBorrador;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.RechazadorDeSolicitudes;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.ResolvedorDatosPublicacion;
import ar.edu.uade.skillswap.aplicacion.comun.ConfiguracionAplicacion;
import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.evento.BusEventos;
import ar.edu.uade.skillswap.aplicacion.evento.GeneradorMensajes;
import ar.edu.uade.skillswap.aplicacion.evento.NotificadorDeEventos;
import ar.edu.uade.skillswap.aplicacion.evento.ObservadorTransaccional;
import ar.edu.uade.skillswap.aplicacion.notificacion.CanalEmail;
import ar.edu.uade.skillswap.aplicacion.notificacion.CanalInterno;
import ar.edu.uade.skillswap.aplicacion.notificacion.CanalPush;
import ar.edu.uade.skillswap.aplicacion.notificacion.FabricaCanales;
import ar.edu.uade.skillswap.dominio.cancelacion.FabricaPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.recomendacion.EstrategiaRecomendacion;

public class ModuloAplicacion {

    public final BusEventos bus = new BusEventos();
    public final EjecutorTransaccional transaccion;
    public final ConsultarBilletera consultarBilletera;
    public final BuscarPublicaciones buscarPublicaciones;
    public final RecomendarPublicaciones recomendarPublicaciones;
    public final CalificarContratacion calificar;
    public final AceptarContratacion aceptar;
    public final CancelarContratacion cancelar;
    public final CompletarContratacion completar;
    public final ConsultarComprobante comprobante;
    public final IniciarContratacion iniciar;
    public final PagarContratacion pagar;
    public final ProponerReprogramacion proponerReprogramacion;
    public final RechazarContratacion rechazar;
    public final ResponderReprogramacion responderReprogramacion;
    public final SolicitarContratacion solicitar;
    public final VencerContratacionesSinPago vencer;
    public final ListarNotificaciones listarNotificaciones;
    public final MarcarNotificacionLeida marcarLeida;
    public final ActualizarPerfil actualizarPerfil;
    public final ConsultarPerfilPublico consultarPerfilPublico;
    public final CrearPerfil crearPerfil;
    public final DarDeBajaPerfil darDeBajaPerfil;
    public final AgregarPromocion agregarPromocion;
    public final CambiarEstadoPublicacion cambiarEstado;
    public final CrearBorrador crearBorrador;
    public final EditarBorrador editarBorrador;

    public ModuloAplicacion(Puertos p, ConfiguracionAplicacion configuracion, EstrategiaRecomendacion estrategia,
                            Clock reloj) {
        transaccion = new EjecutorTransaccional(p.unidadDeTrabajo(), bus);
        FabricaCanales canales = new FabricaCanales(new CanalEmail(p.email()), new CanalPush(p.push()),
                new CanalInterno(p.notificaciones()));
        bus.suscribir(new ObservadorTransaccional(p.unidadDeTrabajo(),
                new NotificadorDeEventos(p.perfiles(), canales, new GeneradorMensajes())));

        RechazadorDeSolicitudes rechazador = new RechazadorDeSolicitudes(p.contrataciones());
        ResolvedorDatosPublicacion resolvedor = new ResolvedorDatosPublicacion(p.catalogo());
        crearPerfil = new CrearPerfil(transaccion, p.perfiles(), p.billeteras(), p.identidad(), configuracion, reloj);
        actualizarPerfil = new ActualizarPerfil(transaccion, p.perfiles(), p.catalogo());
        consultarPerfilPublico = new ConsultarPerfilPublico(transaccion, p.perfiles(), p.consulta(),
                p.contrataciones(), configuracion);
        darDeBajaPerfil = new DarDeBajaPerfil(transaccion, p.perfiles(), p.publicaciones(), p.contrataciones(),
                rechazador, reloj);
        crearBorrador = new CrearBorrador(transaccion, p.perfiles(), p.publicaciones(), resolvedor, reloj);
        editarBorrador = new EditarBorrador(transaccion, p.publicaciones(), resolvedor, reloj);
        agregarPromocion = new AgregarPromocion(transaccion, p.publicaciones(), reloj);
        cambiarEstado = new CambiarEstadoPublicacion(transaccion, p.publicaciones(), p.contrataciones(), rechazador,
                reloj);
        buscarPublicaciones = new BuscarPublicaciones(transaccion, p.consulta());
        recomendarPublicaciones = new RecomendarPublicaciones(transaccion, p.perfiles(), p.consulta(), estrategia);
        solicitar = new SolicitarContratacion(transaccion, p.publicaciones(), p.perfiles(), p.contrataciones(), reloj);
        aceptar = new AceptarContratacion(transaccion, p.contrataciones(), p.publicaciones(), reloj);
        rechazar = new RechazarContratacion(transaccion, p.contrataciones(), reloj);
        pagar = new PagarContratacion(transaccion, p.contrataciones(), p.billeteras(), reloj);
        iniciar = new IniciarContratacion(transaccion, p.contrataciones(), reloj);
        completar = new CompletarContratacion(transaccion, p.contrataciones(), p.billeteras(), reloj);
        cancelar = new CancelarContratacion(transaccion, p.contrataciones(), p.publicaciones(), p.billeteras(),
                FabricaPoliticaCancelacion.porDefecto(), reloj);
        proponerReprogramacion = new ProponerReprogramacion(transaccion, p.contrataciones(), p.publicaciones(), reloj);
        responderReprogramacion = new ResponderReprogramacion(transaccion, p.contrataciones(), p.publicaciones(),
                reloj);
        vencer = new VencerContratacionesSinPago(transaccion, p.contrataciones(), p.publicaciones(), configuracion,
                reloj);
        comprobante = new ConsultarComprobante(transaccion, p.contrataciones());
        calificar = new CalificarContratacion(transaccion, p.contrataciones(), p.calificaciones(), p.perfiles(), reloj);
        consultarBilletera = new ConsultarBilletera(transaccion, p.billeteras());
        listarNotificaciones = new ListarNotificaciones(transaccion, p.notificaciones());
        marcarLeida = new MarcarNotificacionLeida(transaccion, p.notificaciones());
    }
}
