package ar.edu.uade.skillswap.infraestructura.persistencia.repositorio;

import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import ar.edu.uade.skillswap.infraestructura.persistencia.TraductorExcepciones;
import jakarta.persistence.EntityManager;

abstract class RepositorioJpa {

    private final ContextoPersistencia contexto;

    protected RepositorioJpa(ContextoPersistencia contexto) {
        this.contexto = contexto;
    }

    protected EntityManager entityManager() {
        return contexto.entityManager();
    }

    protected void sincronizar() {
        try {
            entityManager().flush();
        } catch (RuntimeException error) {
            throw TraductorExcepciones.traducir(error);
        }
    }
}
