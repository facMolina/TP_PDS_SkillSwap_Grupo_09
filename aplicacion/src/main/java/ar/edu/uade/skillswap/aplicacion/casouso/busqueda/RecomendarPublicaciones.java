package ar.edu.uade.skillswap.aplicacion.casouso.busqueda;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ar.edu.uade.skillswap.aplicacion.busqueda.PublicacionResumen;
import ar.edu.uade.skillswap.aplicacion.comun.EjecutorTransaccional;
import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.puerto.ConsultaPublicaciones;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.recomendacion.CandidataRecomendacion;
import ar.edu.uade.skillswap.dominio.recomendacion.EstrategiaRecomendacion;

public class RecomendarPublicaciones {

    private final EjecutorTransaccional transaccion;
    private final PerfilRepositorio perfiles;
    private final ConsultaPublicaciones consulta;
    private final EstrategiaRecomendacion estrategia;

    public RecomendarPublicaciones(EjecutorTransaccional transaccion, PerfilRepositorio perfiles,
                                   ConsultaPublicaciones consulta, EstrategiaRecomendacion estrategia) {
        this.transaccion = transaccion;
        this.perfiles = perfiles;
        this.consulta = consulta;
        this.estrategia = estrategia;
    }

    public Pagina<PublicacionResumen> ejecutar(RecomendacionConsulta recomendacion) {
        return transaccion.consultar(() -> {
            Perfil perfil = perfiles.obtenerPorUsuario(recomendacion.actor());
            Comparator<CandidataRecomendacion> orden =
                    Comparator.comparingDouble((CandidataRecomendacion candidata) -> estrategia.puntuar(perfil, candidata))
                            .reversed()
                            .thenComparing(Comparator.comparingDouble(CandidataRecomendacion::reputacionAutor).reversed())
                            .thenComparing(CandidataRecomendacion::publicacionId);
            List<Long> ids = consulta.candidatasParaRecomendar(perfil).stream()
                    .sorted(orden)
                    .map(CandidataRecomendacion::publicacionId)
                    .toList();
            Pagina<Long> pagina = Pagina.de(ids, recomendacion.pagina());
            Map<Long, PublicacionResumen> resumenes = consulta.resumenesPorIds(pagina.elementos()).stream()
                    .collect(Collectors.toMap(PublicacionResumen::id, Function.identity()));
            return pagina.transformar(resumenes::get);
        });
    }
}
