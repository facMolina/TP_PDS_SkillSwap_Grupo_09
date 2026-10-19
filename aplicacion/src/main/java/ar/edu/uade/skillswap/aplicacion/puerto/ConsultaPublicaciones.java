package ar.edu.uade.skillswap.aplicacion.puerto;

import java.util.List;

import ar.edu.uade.skillswap.aplicacion.busqueda.FiltroBusqueda;
import ar.edu.uade.skillswap.aplicacion.busqueda.OrdenBusqueda;
import ar.edu.uade.skillswap.aplicacion.busqueda.PublicacionResumen;
import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.recomendacion.CandidataRecomendacion;

public interface ConsultaPublicaciones {

    Pagina<PublicacionResumen> buscar(FiltroBusqueda filtro, OrdenBusqueda orden, SolicitudPagina pagina);

    List<CandidataRecomendacion> candidatasParaRecomendar(Perfil perfil);

    List<PublicacionResumen> resumenesPorIds(List<Long> ids);

    List<PublicacionResumen> publicacionesVigentesDe(UsuarioId autor);
}
