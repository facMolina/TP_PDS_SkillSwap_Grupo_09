package ar.edu.uade.skillswap.dominio.calificacion;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.comun.Identificable;
import ar.edu.uade.skillswap.dominio.comun.Puntaje;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.error.CalificacionNoPermitida;

public class Calificacion implements Identificable {

    private Long id;
    private final Long contratacionId;
    private final UsuarioId autorId;
    private final UsuarioId receptorId;
    private final Puntaje puntaje;
    private final String comentario;
    private final Instant fecha;

    private Calificacion(Long contratacionId, UsuarioId autorId, UsuarioId receptorId, Puntaje puntaje,
                         String comentario, Instant fecha) {
        this.contratacionId = contratacionId;
        this.autorId = autorId;
        this.receptorId = receptorId;
        this.puntaje = puntaje;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public static Calificacion reconstruir(Long id, Long contratacionId, UsuarioId autorId, UsuarioId receptorId,
                                           Puntaje puntaje, String comentario, Instant fecha) {
        Calificacion calificacion = new Calificacion(contratacionId, autorId, receptorId, puntaje, comentario, fecha);
        calificacion.id = id;
        return calificacion;
    }

    public static Calificacion registrar(Contratacion contratacion, UsuarioId autor, Puntaje puntaje,
                                         String comentario, Instant ahora) {
        if (!contratacion.estaCompletada()) {
            throw new CalificacionNoPermitida("Solo se puede calificar una contratación completada");
        }
        if (!contratacion.esParticipante(autor)) {
            throw new CalificacionNoPermitida("Solo los participantes pueden calificar la contratación");
        }
        UsuarioId receptor = contratacion.contraparteDe(autor);
        if (receptor.equals(autor)) {
            throw new CalificacionNoPermitida("Nadie puede calificarse a sí mismo");
        }
        return new Calificacion(contratacion.getId(), autor, receptor, puntaje, comentario, ahora);
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void asignarId(Long id) {
        this.id = id;
    }

    public Long getContratacionId() {
        return contratacionId;
    }

    public UsuarioId getAutorId() {
        return autorId;
    }

    public UsuarioId getReceptorId() {
        return receptorId;
    }

    public Puntaje getPuntaje() {
        return puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public Instant getFecha() {
        return fecha;
    }
}
