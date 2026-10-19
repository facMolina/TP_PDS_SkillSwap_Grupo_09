package ar.edu.uade.skillswap.infraestructura.soporte;

import static ar.edu.uade.skillswap.infraestructura.soporte.EntornoPrueba.INICIO;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.SolicitarComando;
import ar.edu.uade.skillswap.aplicacion.casouso.perfil.CrearPerfilComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CambiarEstadoComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.CrearBorradorComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.DatosPublicacionComando;
import ar.edu.uade.skillswap.aplicacion.casouso.publicacion.PublicacionVista;
import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;
import ar.edu.uade.skillswap.infraestructura.configuracion.ContenedorSkillSwap;

@ExtendWith(RequiereBaseDatos.class)
public abstract class PruebaConBaseDatos {

    protected static final UsuarioId PROVEEDOR = new UsuarioId("proveedor");
    protected static final UsuarioId CLIENTE = new UsuarioId("cliente");
    protected static final UsuarioId OTRO = new UsuarioId("otro");
    protected static final FranjaHoraria F1 = franja("2026-11-10T15:00:00Z", "2026-11-10T16:00:00Z");
    protected static final FranjaHoraria F2 = franja("2026-11-12T15:00:00Z", "2026-11-12T16:00:00Z");
    protected static final FranjaHoraria F3 = franja("2026-11-14T15:00:00Z", "2026-11-14T16:00:00Z");
    protected static final long JAVA = 10L;
    protected static final long SQL = 11L;
    protected static final long CATEGORIA_BACKEND = 2L;

    protected ContenedorSkillSwap app;

    @BeforeEach
    void prepararEntorno() {
        app = EntornoPrueba.contenedor();
        EntornoPrueba.reiniciar();
    }

    protected static FranjaHoraria franja(String inicio, String fin) {
        return new FranjaHoraria(Instant.parse(inicio), Instant.parse(fin));
    }

    protected void registrar(UsuarioId usuario, TipoPerfil tipo) {
        app.aplicacion.crearPerfil.ejecutar(new CrearPerfilComando(usuario, tipo, "Nombre " + usuario.valor(),
                usuario.valor() + "@mail.com", ZoneId.of("UTC")));
    }

    protected void registrarProveedorYCliente() {
        registrar(PROVEEDOR, TipoPerfil.PERSONA);
        registrar(CLIENTE, TipoPerfil.PERSONA);
    }

    protected DatosPublicacionComando datos(String titulo, int cupo, long precio, Set<Long> etiquetas) {
        return new DatosPublicacionComando(TipoPublicacion.MENTORIA, titulo, "Descripción de " + titulo,
                CATEGORIA_BACKEND, Modalidad.REMOTA, 60, cupo, SkillCoins.de(precio), etiquetas,
                TipoPoliticaCancelacion.FLEXIBLE);
    }

    protected Long publicar(UsuarioId autor, DatosPublicacionComando datos, List<FranjaHoraria> franjas) {
        PublicacionVista borrador = app.aplicacion.crearBorrador.ejecutar(new CrearBorradorComando(autor, datos, franjas));
        app.aplicacion.cambiarEstado.ejecutar(
                new CambiarEstadoComando(autor, borrador.id(), AccionPublicacion.PUBLICAR, null));
        return borrador.id();
    }

    protected Long publicacionBasica(int cupo) {
        return publicar(PROVEEDOR, datos("Mentoría Java", cupo, 100, Set.of(JAVA)), List.of(F1, F2, F3));
    }

    protected ContratacionVista solicitar(UsuarioId cliente, Long publicacion, FranjaHoraria franja) {
        return app.aplicacion.solicitar.ejecutar(new SolicitarComando(cliente, publicacion, "Hola", "Practicar", franja));
    }

    protected ContratacionVista aceptar(Long contratacion) {
        return app.aplicacion.aceptar.ejecutar(new ContratacionComando(PROVEEDOR, contratacion));
    }

    protected ContratacionVista pagar(UsuarioId cliente, Long contratacion) {
        return app.aplicacion.pagar.ejecutar(new ContratacionComando(cliente, contratacion));
    }

    protected Instant inicio() {
        return INICIO;
    }
}
