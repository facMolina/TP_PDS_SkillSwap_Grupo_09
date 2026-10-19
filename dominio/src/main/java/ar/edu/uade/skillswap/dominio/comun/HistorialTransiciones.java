package ar.edu.uade.skillswap.dominio.comun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistorialTransiciones {

    private final List<TransicionEstado> transiciones = new ArrayList<>();

    public HistorialTransiciones() {
    }

    public HistorialTransiciones(List<TransicionEstado> existentes) {
        transiciones.addAll(existentes);
    }

    public void registrar(TransicionEstado transicion) {
        transiciones.add(transicion);
    }

    public List<TransicionEstado> getTransiciones() {
        return Collections.unmodifiableList(transiciones);
    }
}
