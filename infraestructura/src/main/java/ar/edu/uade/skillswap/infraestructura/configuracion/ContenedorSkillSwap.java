package ar.edu.uade.skillswap.infraestructura.configuracion;

import java.time.Clock;
import java.time.Duration;
import java.util.Collection;

import ar.edu.uade.skillswap.aplicacion.comun.ConfiguracionAplicacion;
import ar.edu.uade.skillswap.aplicacion.configuracion.ModuloAplicacion;
import ar.edu.uade.skillswap.aplicacion.configuracion.Puertos;
import ar.edu.uade.skillswap.dominio.recomendacion.EstrategiaRecomendacion;
import ar.edu.uade.skillswap.infraestructura.externo.AdaptadorEmailSimulado;
import ar.edu.uade.skillswap.infraestructura.externo.AdaptadorIdentidadSimulada;
import ar.edu.uade.skillswap.infraestructura.externo.AdaptadorPushSimulado;
import ar.edu.uade.skillswap.infraestructura.externo.ClienteCorreoSimulado;
import ar.edu.uade.skillswap.infraestructura.externo.ServicioIdentidadSimulado;
import ar.edu.uade.skillswap.infraestructura.externo.ServicioPushSimulado;
import ar.edu.uade.skillswap.infraestructura.persistencia.ConfiguracionBaseDatos;
import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.FabricaPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.UnidadDeTrabajoJpa;
import ar.edu.uade.skillswap.infraestructura.persistencia.repositorio.BilleteraRepositorioJpa;
import ar.edu.uade.skillswap.infraestructura.persistencia.repositorio.CalificacionRepositorioJpa;
import ar.edu.uade.skillswap.infraestructura.persistencia.repositorio.CatalogoRepositorioJpa;
import ar.edu.uade.skillswap.infraestructura.persistencia.repositorio.ConsultaPublicacionesJpa;
import ar.edu.uade.skillswap.infraestructura.persistencia.repositorio.ContratacionRepositorioJpa;
import ar.edu.uade.skillswap.infraestructura.persistencia.repositorio.NotificacionRepositorioJpa;
import ar.edu.uade.skillswap.infraestructura.persistencia.repositorio.PerfilRepositorioJpa;
import ar.edu.uade.skillswap.infraestructura.persistencia.repositorio.PublicacionRepositorioJpa;
import ar.edu.uade.skillswap.infraestructura.programacion.PlanificadorVencimientos;
import jakarta.persistence.EntityManagerFactory;

public class ContenedorSkillSwap implements AutoCloseable {

    public static final Duration INTERVALO_VENCIMIENTOS = Duration.ofMinutes(5);

    public final Puertos puertos;
    public final ModuloAplicacion aplicacion;
    public final ClienteCorreoSimulado correo = new ClienteCorreoSimulado();
    public final ServicioPushSimulado push = new ServicioPushSimulado();
    public final ServicioIdentidadSimulado identidad;
    public final PlanificadorVencimientos planificador;
    private final EntityManagerFactory fabrica;

    public ContenedorSkillSwap(ConfiguracionBaseDatos baseDatos, ConfiguracionAplicacion configuracion,
                               EstrategiaRecomendacion estrategia, Clock reloj, Collection<String> cuentas) {
        fabrica = FabricaPersistencia.crear(baseDatos);
        identidad = new ServicioIdentidadSimulado(cuentas);
        ContextoPersistencia contexto = new ContextoPersistencia();
        puertos = new Puertos(
                new PerfilRepositorioJpa(contexto),
                new PublicacionRepositorioJpa(contexto),
                new ContratacionRepositorioJpa(contexto),
                new BilleteraRepositorioJpa(contexto),
                new CalificacionRepositorioJpa(contexto),
                new NotificacionRepositorioJpa(contexto),
                new CatalogoRepositorioJpa(contexto),
                new ConsultaPublicacionesJpa(contexto, reloj),
                new AdaptadorIdentidadSimulada(identidad),
                new AdaptadorEmailSimulado(correo),
                new AdaptadorPushSimulado(push),
                new UnidadDeTrabajoJpa(fabrica, contexto));
        aplicacion = new ModuloAplicacion(puertos, configuracion, estrategia, reloj);
        planificador = new PlanificadorVencimientos(aplicacion.vencer, INTERVALO_VENCIMIENTOS);
    }

    public EntityManagerFactory fabricaPersistencia() {
        return fabrica;
    }

    @Override
    public void close() {
        planificador.close();
        fabrica.close();
    }
}
