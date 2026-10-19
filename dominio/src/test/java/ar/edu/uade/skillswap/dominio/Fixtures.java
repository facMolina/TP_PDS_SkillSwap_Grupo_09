package ar.edu.uade.skillswap.dominio;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.catalogo.Categoria;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.dominio.publicacion.DatosPublicacion;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;

public final class Fixtures {

    public static final Instant AHORA = Instant.parse("2026-11-01T12:00:00Z");
    public static final UsuarioId PROVEEDOR = new UsuarioId("proveedor");
    public static final UsuarioId CLIENTE = new UsuarioId("cliente");
    public static final UsuarioId ORGANIZACION = new UsuarioId("organizacion");
    public static final FranjaHoraria FRANJA = franja("2026-11-10T15:00:00Z", "2026-11-10T16:00:00Z");
    public static final FranjaHoraria OTRA_FRANJA = franja("2026-11-12T15:00:00Z", "2026-11-12T16:00:00Z");
    public static final Categoria CATEGORIA = new Categoria(1L, "Programación");
    public static final Habilidad JAVA = new Habilidad(10L, "Java");
    public static final Habilidad SQL = new Habilidad(11L, "SQL");

    private Fixtures() {
    }

    public static FranjaHoraria franja(String inicio, String fin) {
        return new FranjaHoraria(Instant.parse(inicio), Instant.parse(fin));
    }

    public static ContextoAccion ctx(UsuarioId actor) {
        return ContextoAccion.de(actor, AHORA);
    }

    public static ContextoAccion ctx(UsuarioId actor, String instante) {
        return ContextoAccion.de(actor, Instant.parse(instante));
    }

    public static Perfil perfilProveedor() {
        return Perfil.crear(PROVEEDOR, TipoPerfil.PERSONA, "Proveedor", "proveedor@mail.com", ZoneId.of("UTC"));
    }

    public static Perfil perfilCliente() {
        return Perfil.crear(CLIENTE, TipoPerfil.PERSONA, "Cliente", "cliente@mail.com", ZoneId.of("UTC"));
    }

    public static Perfil perfilOrganizacion() {
        return Perfil.crear(ORGANIZACION, TipoPerfil.ORGANIZACION, "Organización", "org@mail.com", ZoneId.of("UTC"));
    }

    public static DatosPublicacion datosCompletos(int cupo, SkillCoins precio) {
        return new DatosPublicacion(TipoPublicacion.MENTORIA, "Mentoría de entrevistas", "Preparación técnica",
                CATEGORIA, Modalidad.REMOTA, 60, cupo, precio, Set.of(JAVA), TipoPoliticaCancelacion.FLEXIBLE);
    }

    public static Publicacion borrador() {
        Publicacion publicacion = Publicacion.crearBorrador(perfilProveedor(), datosCompletos(2, SkillCoins.de(100)), AHORA);
        publicacion.asignarId(1L);
        return publicacion;
    }

    public static Publicacion publicacionPublicada() {
        Publicacion publicacion = borrador();
        publicacion.agregarFranja(FRANJA, ctx(PROVEEDOR));
        publicacion.agregarFranja(OTRA_FRANJA, ctx(PROVEEDOR));
        publicacion.publicar(ctx(PROVEEDOR));
        return publicacion;
    }

    public static Contratacion contratacionPendiente() {
        Contratacion contratacion =
                Contratacion.solicitar(publicacionPublicada(), perfilCliente(), "Hola", "Practicar", FRANJA, AHORA);
        contratacion.asignarId(10L);
        return contratacion;
    }

    public static Contratacion contratacionAceptada() {
        Contratacion contratacion = contratacionPendiente();
        contratacion.aceptar(ctx(PROVEEDOR));
        return contratacion;
    }

    public static Contratacion contratacionConfirmada() {
        Contratacion contratacion = contratacionAceptada();
        contratacion.confirmarPago(ctx(CLIENTE));
        return contratacion;
    }

    public static Contratacion contratacionEnCurso() {
        Contratacion contratacion = contratacionConfirmada();
        contratacion.iniciar(ctx(PROVEEDOR));
        return contratacion;
    }

    public static Contratacion contratacionCompletada() {
        Contratacion contratacion = contratacionEnCurso();
        contratacion.completar(ctx(PROVEEDOR));
        return contratacion;
    }
}
