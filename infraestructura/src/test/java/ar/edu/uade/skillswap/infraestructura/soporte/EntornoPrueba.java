package ar.edu.uade.skillswap.infraestructura.soporte;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import ar.edu.uade.skillswap.aplicacion.comun.ConfiguracionAplicacion;
import ar.edu.uade.skillswap.dominio.recomendacion.RecomendacionPorAfinidad;
import ar.edu.uade.skillswap.infraestructura.configuracion.ContenedorSkillSwap;
import ar.edu.uade.skillswap.infraestructura.persistencia.ConfiguracionBaseDatos;

public final class EntornoPrueba {

    public static final Instant INICIO = Instant.parse("2026-11-01T12:00:00Z");
    public static final RelojPrueba RELOJ = new RelojPrueba(INICIO);
    private static final List<String> TABLAS = List.of("notificacion", "calificacion", "transicion_contratacion",
            "contratacion", "transicion_publicacion", "promocion", "franja_disponible", "publicacion_etiqueta",
            "publicacion", "movimiento_credito", "billetera", "perfil_disponibilidad", "perfil_interes",
            "perfil_habilidad", "perfil");
    private static final List<String> CUENTAS = List.of("proveedor", "cliente", "otro", "organizacion");

    private static ContenedorSkillSwap contenedor;

    private EntornoPrueba() {
    }

    public static synchronized ContenedorSkillSwap contenedor() {
        if (contenedor == null) {
            contenedor = new ContenedorSkillSwap(ConfiguracionBaseDatos.desdeEntorno(),
                    ConfiguracionAplicacion.porDefecto(), new RecomendacionPorAfinidad(), RELOJ, CUENTAS);
            Runtime.getRuntime().addShutdownHook(new Thread(contenedor::close));
        }
        return contenedor;
    }

    public static void reiniciar() {
        RELOJ.fijar(INICIO);
        ConfiguracionBaseDatos configuracion = ConfiguracionBaseDatos.desdeEntorno();
        try (Connection conexion = DriverManager.getConnection(configuracion.url(), configuracion.usuario(),
                configuracion.clave()); Statement sentencia = conexion.createStatement()) {
            sentencia.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String tabla : TABLAS) {
                sentencia.execute("TRUNCATE TABLE " + tabla);
            }
            sentencia.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo limpiar la base de pruebas", error);
        }
    }
}
