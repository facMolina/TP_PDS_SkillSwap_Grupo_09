package ar.edu.uade.skillswap.aplicacion.casouso.billetera;

import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record ConsultaBilletera(UsuarioId actor, SolicitudPagina pagina) {

    public ConsultaBilletera {
        pagina = pagina == null ? SolicitudPagina.primera() : pagina;
    }
}
