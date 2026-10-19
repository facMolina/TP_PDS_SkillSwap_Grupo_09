package ar.edu.uade.skillswap.infraestructura.persistencia;

import org.flywaydb.core.Flyway;

public final class MigradorBaseDatos {

    private MigradorBaseDatos() {
    }

    public static void migrar(ConfiguracionBaseDatos configuracion) {
        Flyway.configure()
                .dataSource(configuracion.url(), configuracion.usuario(), configuracion.clave())
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
