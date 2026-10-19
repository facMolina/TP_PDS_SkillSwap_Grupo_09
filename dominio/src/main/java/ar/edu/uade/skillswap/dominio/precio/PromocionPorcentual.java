package ar.edu.uade.skillswap.dominio.precio;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.error.ValorInvalido;

public class PromocionPorcentual extends Promocion {

    private final int porcentaje;

    public PromocionPorcentual(int porcentaje, Instant vigenteDesde, Instant vigenteHasta) {
        super(vigenteDesde, vigenteHasta);
        if (porcentaje < 1 || porcentaje > 100) {
            throw new ValorInvalido("El porcentaje de la promoción debe estar entre 1 y 100");
        }
        this.porcentaje = porcentaje;
    }

    @Override
    public Precio decorar(Precio precio) {
        return new DescuentoPorcentual(precio, porcentaje);
    }

    public int getPorcentaje() {
        return porcentaje;
    }
}
