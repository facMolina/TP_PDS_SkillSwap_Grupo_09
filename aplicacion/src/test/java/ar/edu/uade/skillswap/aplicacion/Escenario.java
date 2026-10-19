package ar.edu.uade.skillswap.aplicacion;

import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import ar.edu.uade.skillswap.aplicacion.casouso.billetera.ConsultarBilletera;
import ar.edu.uade.skillswap.aplicacion.casouso.busqueda.BuscarPublicaciones;
import ar.edu.uade.skillswap.aplicacion.casouso.calificacion.CalificarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.AceptarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.CancelarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.CompletarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ConsultarComprobante;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.IniciarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.PagarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ProponerReprogramacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.RechazarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ResponderReprogramacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.SolicitarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.SolicitarContratacion;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.VencerContratacionesSinPago;
import ar.edu.uade.skillswap.aplicacion.casouso.notificacion.ListarNotificaciones;
import ar.edu.uade.skillswap.aplicacion.casouso.notificacion.MarcarNotificacionLeida;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.ActualizarPerfil;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.ConsultarPerfilPublico;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.CrearPerfil;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.CrearPerfilComando;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.DarDeBajaPerfil;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AgregarPromocion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CambiarEstadoComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CambiarEstadoPublicacion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CrearBorrador;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CrearBorradorComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.DatosPublicacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.EditarBorrador;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.PublicacionVista;
import ar.edu.uade.skillswap.aplicacion.comun.ConfiguracionAplicacion;
import ar.edu.uade.skillswap.aplicacion.configuracion.ModuloAplicacion;
import ar.edu.uade.skillswap.aplicacion.configuracion.Puertos;
import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.dobles.BilleteraRepositorioEnMemoria;
import ar.edu.uade.skillswap.aplicacion.dobles.CalificacionRepositorioEnMemoria;
import ar.edu.uade.skillswap.aplicacion.dobles.CatalogoRepositorioEnMemoria;
import ar.edu.uade.skillswap.aplicacion.dobles.ContratacionRepositorioEnMemoria;
import ar.edu.uade.skillswap.aplicacion.dobles.NotificacionRepositorioEnMemoria;
import ar.edu.uade.skillswap.aplicacion.dobles.PerfilRepositorioEnMemoria;
import ar.edu.uade.skillswap.aplicacion.dobles.PublicacionRepositorioEnMemoria;
import ar.edu.uade.skillswap.aplicacion.dobles.RelojMutable;
import ar.edu.uade.skillswap.aplicacion.dobles.UnidadDeTrabajoDirecta;
import ar.edu.uade.skillswap.aplicacion.evento.BusEventos;
import ar.edu.uade.skillswap.aplicacion.puerto.ConsultaPublicaciones;
import ar.edu.uade.skillswap.aplicacion.puerto.EnviadorEmail;
import ar.edu.uade.skillswap.aplicacion.puerto.EnviadorPush;
import ar.edu.uade.skillswap.aplicacion.puerto.ServicioIdentidad;
import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;
import ar.edu.uade.skillswap.dominio.recomendacion.EstrategiaRecomendacion;
import ar.edu.uade.skillswap.dominio.recomendacion.RecomendacionPorAfinidad;

public class Escenario {

    public static final UsuarioId PROVEEDOR = new UsuarioId("proveedor");
    public static final UsuarioId CLIENTE = new UsuarioId("cliente");
    public static final UsuarioId ORGANIZACION = new UsuarioId("organizacion");
    public static final Instant INICIO = Instant.parse("2026-11-01T12:00:00Z");
    public static final FranjaHoraria F1 = franja("2026-11-10T15:00:00Z", "2026-11-10T16:00:00Z");
    public static final FranjaHoraria F2 = franja("2026-11-12T15:00:00Z", "2026-11-12T16:00:00Z");
    public static final FranjaHoraria F3 = franja("2026-11-14T15:00:00Z", "2026-11-14T16:00:00Z");
    public static final Habilidad JAVA = new Habilidad(10L, "Java");
    public static final Habilidad SQL = new Habilidad(11L, "SQL");

    public final RelojMutable reloj = new RelojMutable(INICIO);
    public final ConfiguracionAplicacion configuracion;
    public final PerfilRepositorioEnMemoria perfiles = new PerfilRepositorioEnMemoria();
    public final PublicacionRepositorioEnMemoria publicaciones = new PublicacionRepositorioEnMemoria();
    public final ContratacionRepositorioEnMemoria contrataciones = new ContratacionRepositorioEnMemoria();
    public final BilleteraRepositorioEnMemoria billeteras = new BilleteraRepositorioEnMemoria();
    public final CalificacionRepositorioEnMemoria calificaciones = new CalificacionRepositorioEnMemoria();
    public final NotificacionRepositorioEnMemoria notificaciones = new NotificacionRepositorioEnMemoria();
    public final CatalogoRepositorioEnMemoria catalogo = new CatalogoRepositorioEnMemoria();
    public final EnviadorEmail email = mock(EnviadorEmail.class);
    public final EnviadorPush push = mock(EnviadorPush.class);
    public final ConsultaPublicaciones consulta = mock(ConsultaPublicaciones.class);
    public final ServicioIdentidad identidad = usuario -> true;
    public final BusEventos bus;
    public final EjecutorTransaccional transaccion;
    public final Categoria categoria = new Categoria(1L, "Programación");

    public final CrearPerfil crearPerfil;
    public final ActualizarPerfil actualizarPerfil;
    public final ConsultarPerfilPublico consultarPerfilPublico;
    public final DarDeBajaPerfil darDeBajaPerfil;
    public final CrearBorrador crearBorrador;
    public final EditarBorrador editarBorrador;
    public final AgregarPromocion agregarPromocion;
    public final CambiarEstadoPublicacion cambiarEstado;
    public final BuscarPublicaciones buscarPublicaciones;
    public final SolicitarContratacion solicitar;
    public final AceptarContratacion aceptar;
    public final RechazarContratacion rechazar;
    public final PagarContratacion pagar;
    public final IniciarContratacion iniciar;
    public final CompletarContratacion completar;
    public final CancelarContratacion cancelar;
    public final ProponerReprogramacion proponerReprogramacion;
    public final ResponderReprogramacion responderReprogramacion;
    public final VencerContratacionesSinPago vencer;
    public final ConsultarComprobante comprobante;
    public final CalificarContratacion calificar;
    public final ConsultarBilletera consultarBilletera;
    public final ListarNotificaciones listarNotificaciones;
    public final MarcarNotificacionLeida marcarLeida;

    public Escenario() {
        this(ConfiguracionAplicacion.porDefecto());
    }

    public Escenario(ConfiguracionAplicacion configuracion) {
        this.configuracion = configuracion;
        categoria.agregar(JAVA);
        catalogo.agregar(categoria);
        catalogo.agregar(JAVA);
        catalogo.agregar(SQL);

        Puertos puertos = new Puertos(perfiles, publicaciones, contrataciones, billeteras, calificaciones,
                notificaciones, catalogo, consulta, identidad, email, push, new UnidadDeTrabajoDirecta());
        ModuloAplicacion modulo = new ModuloAplicacion(puertos, configuracion, estrategiaPorDefecto(), reloj);
        bus = modulo.bus;
        transaccion = modulo.transaccion;
        crearPerfil = modulo.crearPerfil;
        actualizarPerfil = modulo.actualizarPerfil;
        consultarPerfilPublico = modulo.consultarPerfilPublico;
        darDeBajaPerfil = modulo.darDeBajaPerfil;
        crearBorrador = modulo.crearBorrador;
        editarBorrador = modulo.editarBorrador;
        agregarPromocion = modulo.agregarPromocion;
        cambiarEstado = modulo.cambiarEstado;
        buscarPublicaciones = modulo.buscarPublicaciones;
        solicitar = modulo.solicitar;
        aceptar = modulo.aceptar;
        rechazar = modulo.rechazar;
        pagar = modulo.pagar;
        iniciar = modulo.iniciar;
        completar = modulo.completar;
        cancelar = modulo.cancelar;
        proponerReprogramacion = modulo.proponerReprogramacion;
        responderReprogramacion = modulo.responderReprogramacion;
        vencer = modulo.vencer;
        comprobante = modulo.comprobante;
        calificar = modulo.calificar;
        consultarBilletera = modulo.consultarBilletera;
        listarNotificaciones = modulo.listarNotificaciones;
        marcarLeida = modulo.marcarLeida;
    }

    public static FranjaHoraria franja(String inicio, String fin) {
        return new FranjaHoraria(Instant.parse(inicio), Instant.parse(fin));
    }

    public Escenario conPerfiles() {
        registrar(PROVEEDOR, TipoPerfil.PERSONA);
        registrar(CLIENTE, TipoPerfil.PERSONA);
        return this;
    }

    public void registrar(UsuarioId usuario, TipoPerfil tipo) {
        crearPerfil.ejecutar(new CrearPerfilComando(usuario, tipo, "Nombre " + usuario.valor(),
                usuario.valor() + "@mail.com", ZoneId.of("UTC")));
    }

    public DatosPublicacionComando datos(int cupo, long precio, TipoPoliticaCancelacion politica) {
        return new DatosPublicacionComando(TipoPublicacion.MENTORIA, "Mentoría", "Descripción", 1L, Modalidad.REMOTA,
                60, cupo, SkillCoins.de(precio), Set.of(10L), politica);
    }

    public Long publicacionPublicada(int cupo, long precio, TipoPoliticaCancelacion politica) {
        PublicacionVista borrador = crearBorrador.ejecutar(
                new CrearBorradorComando(PROVEEDOR, datos(cupo, precio, politica), List.of(F1, F2, F3)));
        cambiarEstado.ejecutar(new CambiarEstadoComando(PROVEEDOR, borrador.id(), AccionPublicacion.PUBLICAR, null));
        return borrador.id();
    }

    public Long publicacionPublicada() {
        return publicacionPublicada(3, 100, TipoPoliticaCancelacion.FLEXIBLE);
    }

    public ContratacionVista solicitarFranja(Long publicacionId, FranjaHoraria franja) {
        return solicitar.ejecutar(new SolicitarComando(CLIENTE, publicacionId, "Hola", "Practicar", franja));
    }

    public ContratacionVista confirmada(Long publicacionId, FranjaHoraria franja) {
        ContratacionVista vista = solicitarFranja(publicacionId, franja);
        aceptar.ejecutar(new ContratacionComando(PROVEEDOR, vista.id()));
        return pagar.ejecutar(new ContratacionComando(CLIENTE, vista.id()));
    }

    public ContratacionVista completada(Long publicacionId) {
        ContratacionVista vista = confirmada(publicacionId, F1);
        iniciar.ejecutar(new ContratacionComando(PROVEEDOR, vista.id()));
        return completar.ejecutar(new ContratacionComando(PROVEEDOR, vista.id()));
    }

    public SkillCoins saldoDe(UsuarioId usuario) {
        return billeteras.obtenerPorUsuario(usuario).getSaldo();
    }

    public EstrategiaRecomendacion estrategiaPorDefecto() {
        return new RecomendacionPorAfinidad();
    }
}
