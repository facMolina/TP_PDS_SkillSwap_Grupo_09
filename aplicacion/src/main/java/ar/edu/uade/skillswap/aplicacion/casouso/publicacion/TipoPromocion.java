package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.precio.Promocion;
import ar.edu.uade.skillswap.dominio.precio.PromocionMontoFijo;
import ar.edu.uade.skillswap.dominio.precio.PromocionPorcentual;

public enum TipoPromocion {
    PORCENTAJE {
        @Override
        public Promocion crear(long valor, Instant desde, Instant hasta) {
            return new PromocionPorcentual((int) valor, desde, hasta);
        }
    },
    MONTO_FIJO {
        @Override
        public Promocion crear(long valor, Instant desde, Instant hasta) {
            return new PromocionMontoFijo(SkillCoins.de(valor), desde, hasta);
        }
    };

    public abstract Promocion crear(long valor, Instant desde, Instant hasta);
}
