package ar.edu.uade.skillswap.dominio.contratacion;

public class EstadoCompletada extends EstadoContratacionBase {

    @Override
    public String getNombre() {
        return "COMPLETADA";
    }

    @Override
    public boolean estaCompletada() {
        return true;
    }
}
