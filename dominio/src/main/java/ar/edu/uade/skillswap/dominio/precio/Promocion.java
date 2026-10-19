package ar.edu.uade.skillswap.dominio.precio;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.comun.Identificable;

public abstract class Promocion implements Identificable {

    private Long id;
    private final Instant vigenteDesde;
    private final Instant vigenteHasta;

    protected Promocion(Instant vigenteDesde, Instant vigenteHasta) {
        this.vigenteDesde = vigenteDesde;
        this.vigenteHasta = vigenteHasta;
    }

    public boolean estaVigente(Instant ahora) {
        return !ahora.isBefore(vigenteDesde) && ahora.isBefore(vigenteHasta);
    }

    public abstract Precio decorar(Precio precio);

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void asignarId(Long id) {
        this.id = id;
    }

    public Instant getVigenteDesde() {
        return vigenteDesde;
    }

    public Instant getVigenteHasta() {
        return vigenteHasta;
    }
}
