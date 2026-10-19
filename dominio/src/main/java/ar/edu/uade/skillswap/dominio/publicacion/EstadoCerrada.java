package ar.edu.uade.skillswap.dominio.publicacion;

public class EstadoCerrada extends EstadoPublicacionBase {

    @Override
    public String getNombre() {
        return "CERRADA";
    }

    @Override
    public boolean esFinal() {
        return true;
    }
}
