package ar.edu.uade.skillswap.infraestructura.persistencia;

import java.util.function.Supplier;

import ar.edu.uade.skillswap.aplicacion.puerto.UnidadDeTrabajo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class UnidadDeTrabajoJpa implements UnidadDeTrabajo {

    private final EntityManagerFactory fabrica;
    private final ContextoPersistencia contexto;

    public UnidadDeTrabajoJpa(EntityManagerFactory fabrica, ContextoPersistencia contexto) {
        this.fabrica = fabrica;
        this.contexto = contexto;
    }

    @Override
    public <T> T ejecutar(Supplier<T> accion) {
        if (contexto.hayUnidadActiva()) {
            return accion.get();
        }
        EntityManager entityManager = fabrica.createEntityManager();
        contexto.vincular(entityManager);
        EntityTransaction transaccion = entityManager.getTransaction();
        try {
            transaccion.begin();
            T resultado = accion.get();
            transaccion.commit();
            return resultado;
        } catch (RuntimeException error) {
            revertir(transaccion);
            throw TraductorExcepciones.traducir(error);
        } finally {
            contexto.desvincular();
            entityManager.close();
        }
    }

    private void revertir(EntityTransaction transaccion) {
        if (transaccion.isActive()) {
            transaccion.rollback();
        }
    }
}
