package ar.edu.uade.skillswap.infraestructura.persistencia;

import jakarta.persistence.EntityManager;

public class ContextoPersistencia {

    private final ThreadLocal<EntityManager> vinculado = new ThreadLocal<>();

    public boolean hayUnidadActiva() {
        return vinculado.get() != null;
    }

    public EntityManager entityManager() {
        EntityManager entityManager = vinculado.get();
        if (entityManager == null) {
            throw new IllegalStateException("La operación requiere una unidad de trabajo activa");
        }
        return entityManager;
    }

    void vincular(EntityManager entityManager) {
        vinculado.set(entityManager);
    }

    void desvincular() {
        vinculado.remove();
    }
}
