package ar.edu.uade.skillswap.aplicacion.casouso.perfil;

import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

public record ResumenContratacion(Long contratacionId, String estado, SkillCoins monto, FranjaHoraria franja) {

    public static ResumenContratacion de(Contratacion contratacion) {
        return new ResumenContratacion(contratacion.getId(), contratacion.getNombreEstado(),
                contratacion.getPrecioAcordado(), contratacion.getFranja());
    }
}
