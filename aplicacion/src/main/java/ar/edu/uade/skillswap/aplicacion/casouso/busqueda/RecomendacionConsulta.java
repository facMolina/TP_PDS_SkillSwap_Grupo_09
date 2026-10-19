package ar.edu.uade.skillswap.aplicacion.casouso.busqueda;

import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record RecomendacionConsulta(UsuarioId actor, SolicitudPagina pagina) {

    public RecomendacionConsulta {
        pagina = pagina == null ? SolicitudPagina.primera() : pagina;
    }
}
