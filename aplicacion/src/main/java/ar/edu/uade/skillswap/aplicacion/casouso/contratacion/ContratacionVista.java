package ar.edu.uade.skillswap.aplicacion.casouso.contratacion;

import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

public record ContratacionVista(Long id, Long publicacionId, UsuarioId cliente, UsuarioId proveedor, String estado,
                                FranjaHoraria franja, SkillCoins precioAcordado, SkillCoins montoRetenido,
                                boolean reprogramacionPendiente) {

    public static ContratacionVista de(Contratacion contratacion) {
        return new ContratacionVista(contratacion.getId(), contratacion.getPublicacionId(),
                contratacion.getClienteId(), contratacion.getProveedorId(), contratacion.getNombreEstado(),
                contratacion.getFranja(), contratacion.getPrecioAcordado(), contratacion.getMontoRetenido(),
                contratacion.getPropuesta() != null && contratacion.getPropuesta().estaPendiente());
    }
}
