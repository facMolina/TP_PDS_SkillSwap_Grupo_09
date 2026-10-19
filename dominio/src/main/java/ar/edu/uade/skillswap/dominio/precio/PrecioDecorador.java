package ar.edu.uade.skillswap.dominio.precio;

public abstract class PrecioDecorador implements Precio {

    protected final Precio precio;

    protected PrecioDecorador(Precio precio) {
        this.precio = precio;
    }
}
