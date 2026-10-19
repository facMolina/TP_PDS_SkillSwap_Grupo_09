package ar.edu.uade.skillswap.dominio.contratacion;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ar.edu.uade.skillswap.dominio.cancelacion.PoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.HistorialTransiciones;
import ar.edu.uade.skillswap.dominio.comun.Identificable;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.TransicionEstado;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.error.FranjaNoDisponible;
import ar.edu.uade.skillswap.dominio.error.PermisoDenegado;
import ar.edu.uade.skillswap.dominio.error.SolicitudNoPermitida;
import ar.edu.uade.skillswap.dominio.error.TransicionInvalida;
import ar.edu.uade.skillswap.dominio.error.ValorInvalido;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public class Contratacion implements Identificable {

    private Long id;
    private final Long publicacionId;
    private final UsuarioId clienteId;
    private final UsuarioId proveedorId;
    private final String mensaje;
    private final String objetivo;
    private FranjaHoraria franja;
    private final SkillCoins precioAcordado;
    private SkillCoins montoRetenido = SkillCoins.CERO;
    private final TipoPoliticaCancelacion tipoPoliticaCancelacion;
    private final Instant fechaSolicitud;
    private Instant fechaAceptacion;
    private Instant fechaCompletada;
    private PropuestaReprogramacion propuesta;
    private EstadoContratacion estadoActual = new EstadoPendiente();
    private final HistorialTransiciones historial;
    private final List<EventoPendiente> eventos = new ArrayList<>();

    private record EventoPendiente(TipoEvento tipo, UsuarioId actor, List<UsuarioId> destinatarios, Instant fecha) {
    }

    private Contratacion(Publicacion publicacion, UsuarioId clienteId, String mensaje, String objetivo,
                         FranjaHoraria franja, SkillCoins precioAcordado, Instant fechaSolicitud) {
        this.publicacionId = publicacion.getId();
        this.clienteId = clienteId;
        this.proveedorId = publicacion.getAutorId();
        this.mensaje = mensaje;
        this.objetivo = objetivo;
        this.franja = franja;
        this.precioAcordado = precioAcordado;
        this.tipoPoliticaCancelacion = publicacion.getTipoPoliticaCancelacion();
        this.fechaSolicitud = fechaSolicitud;
        this.historial = new HistorialTransiciones();
    }

    private Contratacion(InstantaneaContratacion i) {
        this.id = i.id();
        this.publicacionId = i.publicacionId();
        this.clienteId = i.clienteId();
        this.proveedorId = i.proveedorId();
        this.mensaje = i.mensaje();
        this.objetivo = i.objetivo();
        this.franja = i.franja();
        this.precioAcordado = i.precioAcordado();
        this.montoRetenido = i.montoRetenido();
        this.tipoPoliticaCancelacion = i.tipoPoliticaCancelacion();
        this.fechaSolicitud = i.fechaSolicitud();
        this.fechaAceptacion = i.fechaAceptacion();
        this.fechaCompletada = i.fechaCompletada();
        this.propuesta = i.propuesta();
        this.estadoActual = EstadosContratacion.porNombre(i.estado());
        this.historial = new HistorialTransiciones(i.historial());
    }

    public static Contratacion reconstruir(InstantaneaContratacion instantanea) {
        return new Contratacion(instantanea);
    }

    public static Contratacion solicitar(Publicacion publicacion, Perfil cliente, String mensaje, String objetivo,
                                         FranjaHoraria franja, Instant ahora) {
        if (!cliente.estaActivo()) {
            throw new SolicitudNoPermitida("El perfil está dado de baja");
        }
        if (publicacion.getAutorId().equals(cliente.getUsuarioId())) {
            throw new SolicitudNoPermitida("El autor no puede solicitar su propia publicación");
        }
        if (!publicacion.admiteSolicitudes()) {
            throw new SolicitudNoPermitida("La publicación no admite solicitudes");
        }
        if (!publicacion.tieneFranjaLibre(franja)) {
            throw new FranjaNoDisponible("La franja elegida no está disponible");
        }
        Contratacion contratacion = new Contratacion(publicacion, cliente.getUsuarioId(), mensaje, objetivo, franja,
                publicacion.calcularPrecio(ahora), ahora);
        ContextoAccion ctx = ContextoAccion.de(cliente.getUsuarioId(), ahora);
        contratacion.historial.registrar(
                new TransicionEstado(ahora, cliente.getUsuarioId(), "NINGUNO", "PENDIENTE", "Solicitud"));
        contratacion.registrarEvento(TipoEvento.SOLICITUD_RECIBIDA, ctx, List.of(contratacion.proveedorId));
        return contratacion;
    }

    public void aceptar(ContextoAccion ctx) {
        estadoActual.aceptar(this, ctx);
    }

    public void rechazar(ContextoAccion ctx, String motivo) {
        exigirMotivo(motivo);
        estadoActual.rechazar(this, ctx, motivo);
    }

    public void confirmarPago(ContextoAccion ctx) {
        estadoActual.confirmarPago(this, ctx);
    }

    public void iniciar(ContextoAccion ctx) {
        estadoActual.iniciar(this, ctx);
    }

    public SkillCoins completar(ContextoAccion ctx) {
        return estadoActual.completar(this, ctx);
    }

    public ResultadoCancelacion cancelar(ContextoAccion ctx, PoliticaCancelacion politica, String motivo) {
        exigirMotivo(motivo);
        return estadoActual.cancelar(this, ctx, politica, motivo);
    }

    public boolean vencerSinPago(Instant ahora, Duration plazo) {
        return estadoActual.vencerSinPago(this, ahora, plazo);
    }

    public void proponerReprogramacion(ContextoAccion ctx, FranjaHoraria nuevaFranja) {
        estadoActual.proponerReprogramacion(this, ctx, nuevaFranja);
    }

    public void responderReprogramacion(ContextoAccion ctx, boolean acepta) {
        estadoActual.responderReprogramacion(this, ctx, acepta);
    }

    public Comprobante generarComprobante() {
        if (!estadoActual.estaCompletada()) {
            throw new TransicionInvalida("El comprobante solo está disponible para contrataciones completadas");
        }
        return new Comprobante(id, clienteId, proveedorId, precioAcordado, fechaCompletada);
    }

    public boolean esProveedor(UsuarioId usuario) {
        return proveedorId.equals(usuario);
    }

    public boolean esCliente(UsuarioId usuario) {
        return clienteId.equals(usuario);
    }

    public boolean esParticipante(UsuarioId usuario) {
        return esProveedor(usuario) || esCliente(usuario);
    }

    public UsuarioId contraparteDe(UsuarioId usuario) {
        return esProveedor(usuario) ? clienteId : proveedorId;
    }

    public boolean estaComprometida() {
        return estadoActual.estaComprometida();
    }

    public boolean estaCompletada() {
        return estadoActual.estaCompletada();
    }

    public void cambiarEstado(EstadoContratacion nuevo, ContextoAccion ctx, String motivo) {
        historial.registrar(
                new TransicionEstado(ctx.ahora(), ctx.actor(), estadoActual.getNombre(), nuevo.getNombre(), motivo));
        estadoActual = nuevo;
    }

    public List<EventoContratacion> extraerEventos() {
        List<EventoContratacion> extraidos = eventos.stream()
                .map(e -> new EventoContratacion(e.tipo(), id, e.actor(), e.destinatarios(), e.fecha()))
                .toList();
        eventos.clear();
        return extraidos;
    }

    void exigirProveedor(ContextoAccion ctx) {
        if (!esProveedor(ctx.actor())) {
            throw new PermisoDenegado("Solo el proveedor puede realizar esta acción");
        }
    }

    void exigirProveedorOSistema(ContextoAccion ctx) {
        if (!esProveedor(ctx.actor()) && !UsuarioId.SISTEMA.equals(ctx.actor())) {
            throw new PermisoDenegado("Solo el proveedor puede realizar esta acción");
        }
    }

    void exigirCliente(ContextoAccion ctx) {
        if (!esCliente(ctx.actor())) {
            throw new PermisoDenegado("Solo el cliente puede realizar esta acción");
        }
    }

    void exigirParticipante(ContextoAccion ctx) {
        if (!esParticipante(ctx.actor())) {
            throw new PermisoDenegado("Solo los participantes pueden realizar esta acción");
        }
    }

    void registrarAceptacion(Instant ahora) {
        fechaAceptacion = ahora;
    }

    void registrarCompletada(Instant ahora) {
        fechaCompletada = ahora;
    }

    void retenerPago() {
        montoRetenido = precioAcordado;
    }

    SkillCoins liberarRetencion() {
        SkillCoins liberado = montoRetenido;
        montoRetenido = SkillCoins.CERO;
        return liberado;
    }

    Duration anticipacionDesde(Instant ahora) {
        Duration anticipacion = Duration.between(ahora, franja.inicio());
        return anticipacion.isNegative() ? Duration.ZERO : anticipacion;
    }

    List<UsuarioId> ambasPartes() {
        return List.of(clienteId, proveedorId);
    }

    void abrirPropuesta(ContextoAccion ctx, FranjaHoraria nuevaFranja) {
        exigirParticipante(ctx);
        if (propuesta != null && propuesta.estaPendiente()) {
            throw new TransicionInvalida("Ya existe una propuesta de reprogramación pendiente");
        }
        propuesta = new PropuestaReprogramacion(ctx.actor(), nuevaFranja);
        registrarEvento(TipoEvento.REPROGRAMACION_PROPUESTA, ctx, List.of(contraparteDe(ctx.actor())));
    }

    void resolverPropuesta(ContextoAccion ctx, boolean acepta) {
        exigirParticipante(ctx);
        if (propuesta == null || !propuesta.estaPendiente()) {
            throw new TransicionInvalida("No hay una propuesta de reprogramación pendiente");
        }
        if (propuesta.getPropuestaPor().equals(ctx.actor())) {
            throw new PermisoDenegado("La propuesta debe ser respondida por la otra parte");
        }
        if (acepta) {
            propuesta.aceptar();
            franja = propuesta.getNuevaFranja();
        } else {
            propuesta.rechazar();
        }
        registrarEvento(TipoEvento.REPROGRAMACION_RESUELTA, ctx, List.of(propuesta.getPropuestaPor()));
    }

    void registrarEvento(TipoEvento tipo, ContextoAccion ctx, List<UsuarioId> destinatarios) {
        eventos.add(new EventoPendiente(tipo, ctx.actor(), destinatarios, ctx.ahora()));
    }

    private void exigirMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ValorInvalido("El motivo es obligatorio");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void asignarId(Long id) {
        this.id = id;
    }

    public Long getPublicacionId() {
        return publicacionId;
    }

    public UsuarioId getClienteId() {
        return clienteId;
    }

    public UsuarioId getProveedorId() {
        return proveedorId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public FranjaHoraria getFranja() {
        return franja;
    }

    public SkillCoins getPrecioAcordado() {
        return precioAcordado;
    }

    public SkillCoins getMontoRetenido() {
        return montoRetenido;
    }

    public TipoPoliticaCancelacion getTipoPoliticaCancelacion() {
        return tipoPoliticaCancelacion;
    }

    public Instant getFechaSolicitud() {
        return fechaSolicitud;
    }

    public Instant getFechaAceptacion() {
        return fechaAceptacion;
    }

    public Instant getFechaCompletada() {
        return fechaCompletada;
    }

    public PropuestaReprogramacion getPropuesta() {
        return propuesta;
    }

    public String getNombreEstado() {
        return estadoActual.getNombre();
    }

    public HistorialTransiciones getHistorial() {
        return historial;
    }
}
