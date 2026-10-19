package ar.edu.uade.skillswap.infraestructura.persistencia.mapeo;

import java.util.List;

import ar.edu.uade.skillswap.dominio.comun.TransicionEstado;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.TransicionEmbebida;

final class TransicionMapeador {

    private TransicionMapeador() {
    }

    static List<TransicionEstado> aDominio(List<TransicionEmbebida> embebidas) {
        return embebidas.stream()
                .map(t -> new TransicionEstado(t.fecha, new UsuarioId(t.actorId), t.origen, t.destino, t.motivo))
                .toList();
    }

    static void copiar(List<TransicionEstado> origen, List<TransicionEmbebida> destino) {
        destino.clear();
        origen.forEach(t -> destino.add(aEmbebida(t)));
    }

    private static TransicionEmbebida aEmbebida(TransicionEstado t) {
        TransicionEmbebida e = new TransicionEmbebida();
        e.fecha = t.fecha();
        e.actorId = t.actorId().valor();
        e.origen = t.origen();
        e.destino = t.destino();
        e.motivo = t.motivo();
        return e;
    }
}
