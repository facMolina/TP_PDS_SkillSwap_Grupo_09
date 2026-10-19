package ar.edu.uade.skillswap.infraestructura.persistencia;

import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.BilleteraEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.CalificacionEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.ContratacionEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.MovimientoCreditoEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.NodoCatalogoEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.NotificacionEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.PerfilEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.PublicacionEntidad;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.cfg.Configuration;

public final class FabricaPersistencia {

    private FabricaPersistencia() {
    }

    public static EntityManagerFactory crear(ConfiguracionBaseDatos configuracion) {
        MigradorBaseDatos.migrar(configuracion);
        Configuration hibernate = new Configuration()
                .setProperty("hibernate.connection.url", configuracion.url())
                .setProperty("hibernate.connection.username", configuracion.usuario())
                .setProperty("hibernate.connection.password", configuracion.clave())
                .setProperty("hibernate.connection.provider_class",
                        "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
                .setProperty("hibernate.hikari.maximumPoolSize", "10")
                .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
                .setProperty("hibernate.hbm2ddl.auto", "validate")
                .setProperty("hibernate.jdbc.time_zone", "UTC")
                .setProperty("hibernate.type.preferred_instant_jdbc_type", "TIMESTAMP")
                .setProperty("hibernate.show_sql", "false");
        for (Class<?> entidad : new Class<?>[] {NodoCatalogoEntidad.class, PerfilEntidad.class,
                BilleteraEntidad.class, MovimientoCreditoEntidad.class, PublicacionEntidad.class,
                ContratacionEntidad.class, CalificacionEntidad.class, NotificacionEntidad.class}) {
            hibernate.addAnnotatedClass(entidad);
        }
        return hibernate.buildSessionFactory();
    }
}
