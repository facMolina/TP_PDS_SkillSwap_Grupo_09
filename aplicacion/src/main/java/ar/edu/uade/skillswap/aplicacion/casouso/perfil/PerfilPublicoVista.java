package ar.edu.uade.skillswap.aplicacion.casouso.perfil;

import java.util.List;

import ar.edu.uade.skillswap.aplicacion.busqueda.PublicacionResumen;

public record PerfilPublicoVista(PerfilVista perfil, List<PublicacionResumen> publicacionesVigentes,
                                 List<ResumenContratacion> historial) {
}
