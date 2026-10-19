package ar.edu.uade.skillswap.infraestructura.persistencia;

import ar.edu.uade.skillswap.aplicacion.error.ConflictoDeConcurrencia;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.StaleStateException;
import org.hibernate.exception.LockAcquisitionException;

public final class TraductorExcepciones {

    private TraductorExcepciones() {
    }

    public static RuntimeException traducir(RuntimeException error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (esConflictoDeConcurrencia(causa)) {
                return new ConflictoDeConcurrencia("El recurso fue modificado por otra transacción");
            }
        }
        return error;
    }

    private static boolean esConflictoDeConcurrencia(Throwable causa) {
        return causa instanceof OptimisticLockException
                || causa instanceof StaleObjectStateException
                || causa instanceof StaleStateException
                || causa instanceof LockAcquisitionException;
    }
}
