package ar.edu.uade.skillswap.dominio.publicacion;

public class EstadoCancelada extends EstadoPublicacionBase {

    @Override
    public String getNombre() {
        return "CANCELADA";
    }

    @Override
    public boolean esFinal() {
        return true;
    }
}
