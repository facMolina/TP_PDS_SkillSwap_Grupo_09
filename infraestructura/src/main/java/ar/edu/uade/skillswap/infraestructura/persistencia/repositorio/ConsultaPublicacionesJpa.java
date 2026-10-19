package ar.edu.uade.skillswap.infraestructura.persistencia.repositorio;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ar.edu.uade.skillswap.aplicacion.busqueda.FiltroBusqueda;
import ar.edu.uade.skillswap.aplicacion.busqueda.OrdenBusqueda;
import ar.edu.uade.skillswap.aplicacion.busqueda.PublicacionResumen;
import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.puerto.ConsultaPublicaciones;
import ar.edu.uade.skillswap.dominio.catalogo.Habilidad;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.recomendacion.CandidataRecomendacion;
import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import jakarta.persistence.Query;

public class ConsultaPublicacionesJpa extends RepositorioJpa implements ConsultaPublicaciones {

    private static final int PESO_TITULO = 3;
    private static final int PESO_ETIQUETA = 2;
    private static final int PESO_DESCRIPCION = 1;
    private static final Duration VENTANA_POPULARIDAD = Duration.ofDays(30);

    private static final String COLUMNAS_RESUMEN = """
            p.id, p.titulo, a.nombre_visible, COALESCE(p.precio_base, 0),
            COALESCE(a.suma_puntajes / NULLIF(a.cantidad_calificaciones, 0), 0)""";
    private static final String ORIGEN_PUBLICADAS = """
            FROM publicacion p JOIN perfil a ON a.usuario_id = p.autor_id
            WHERE p.estado = 'PUBLICADA' AND a.estado = 'ACTIVO'""";

    private final Clock reloj;

    public ConsultaPublicacionesJpa(ContextoPersistencia contexto, Clock reloj) {
        super(contexto);
        this.reloj = reloj;
    }

    @Override
    public Pagina<PublicacionResumen> buscar(FiltroBusqueda filtro, OrdenBusqueda orden, SolicitudPagina pagina) {
        Map<String, Object> parametros = new HashMap<>();
        String condiciones = condiciones(filtro, parametros);
        String relevancia = tieneTexto(filtro) ? coincidenciaTexto() : "0";

        Query conteo = entityManager().createNativeQuery("SELECT COUNT(*) " + ORIGEN_PUBLICADAS + condiciones);
        asignar(conteo, parametros);
        long total = ((Number) conteo.getSingleResult()).longValue();

        String sql = "SELECT " + COLUMNAS_RESUMEN + ", " + relevancia + " AS relevancia "
                + ORIGEN_PUBLICADAS + condiciones + " ORDER BY " + ordenamiento(orden)
                + " LIMIT :limite OFFSET :desplazamiento";
        Query consulta = entityManager().createNativeQuery(sql);
        asignar(consulta, parametros);
        consulta.setParameter("limite", pagina.tamanio());
        consulta.setParameter("desplazamiento", pagina.desplazamiento());
        List<PublicacionResumen> resumenes = filas(consulta).stream().map(this::aResumen).toList();
        return new Pagina<>(resumenes, pagina.numero(), pagina.tamanio(), total);
    }

    @Override
    public List<CandidataRecomendacion> candidatasParaRecomendar(Perfil perfil) {
        Instant desde = reloj.instant().minus(VENTANA_POPULARIDAD);
        Query consulta = entityManager().createNativeQuery("""
                SELECT p.id, COALESCE(a.suma_puntajes / NULLIF(a.cantidad_calificaciones, 0), 0),
                       (SELECT COUNT(*) FROM contratacion c
                         WHERE c.publicacion_id = p.id AND c.fecha_solicitud >= :desde)
                """ + ORIGEN_PUBLICADAS + " AND p.autor_id <> :autor ORDER BY p.id");
        consulta.setParameter("desde", desde);
        consulta.setParameter("autor", perfil.getUsuarioId().valor());
        List<Object[]> filas = filas(consulta);
        Map<Long, Set<Habilidad>> etiquetas = etiquetasDe(filas.stream().map(f -> ((Number) f[0]).longValue()).toList());
        return filas.stream()
                .map(f -> {
                    Long id = ((Number) f[0]).longValue();
                    return new CandidataRecomendacion(id, etiquetas.getOrDefault(id, Set.of()),
                            ((Number) f[2]).intValue(), redondear(((Number) f[1]).doubleValue()));
                })
                .toList();
    }

    @Override
    public List<PublicacionResumen> resumenesPorIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Query consulta = entityManager().createNativeQuery("SELECT " + COLUMNAS_RESUMEN
                + " FROM publicacion p JOIN perfil a ON a.usuario_id = p.autor_id WHERE p.id IN (:ids)");
        consulta.setParameter("ids", ids);
        Map<Long, PublicacionResumen> porId = new HashMap<>();
        filas(consulta).stream().map(this::aResumen).forEach(resumen -> porId.put(resumen.id(), resumen));
        return ids.stream().map(porId::get).filter(Objects::nonNull).toList();
    }

    @Override
    public List<PublicacionResumen> publicacionesVigentesDe(UsuarioId autor) {
        Query consulta = entityManager().createNativeQuery("SELECT " + COLUMNAS_RESUMEN + " "
                + ORIGEN_PUBLICADAS + " AND p.autor_id = :autor ORDER BY p.id");
        consulta.setParameter("autor", autor.valor());
        return filas(consulta).stream().map(this::aResumen).toList();
    }

    private String condiciones(FiltroBusqueda filtro, Map<String, Object> parametros) {
        StringBuilder sql = new StringBuilder();
        if (tieneTexto(filtro)) {
            sql.append(" AND ").append(coincidenciaTexto()).append(" > 0");
            parametros.put("texto", patron(filtro.texto()));
        }
        if (filtro.categoria() != null) {
            sql.append(" AND p.categoria_id IN (:categorias)");
            parametros.put("categorias", filtro.categoriasIncluidas());
        }
        if (filtro.modalidad() != null) {
            sql.append(" AND p.modalidad = :modalidad");
            parametros.put("modalidad", filtro.modalidad().name());
        }
        if (filtro.precioMinimo() != null) {
            sql.append(" AND p.precio_base >= :precioMinimo");
            parametros.put("precioMinimo", filtro.precioMinimo().monto());
        }
        if (filtro.precioMaximo() != null) {
            sql.append(" AND p.precio_base <= :precioMaximo");
            parametros.put("precioMaximo", filtro.precioMaximo().monto());
        }
        if (filtro.habilidad() != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM publicacion_etiqueta e WHERE e.publicacion_id = p.id "
                    + "AND e.habilidad_id = :habilidad)");
            parametros.put("habilidad", filtro.habilidad().id());
        }
        if (filtro.reputacionMinima() != null) {
            sql.append(" AND COALESCE(a.suma_puntajes / NULLIF(a.cantidad_calificaciones, 0), 0) >= :reputacionMinima");
            parametros.put("reputacionMinima", filtro.reputacionMinima());
        }
        return sql.toString();
    }

    private String coincidenciaTexto() {
        return "(CASE WHEN LOWER(p.titulo) LIKE :texto THEN " + PESO_TITULO + " ELSE 0 END"
                + " + CASE WHEN EXISTS (SELECT 1 FROM publicacion_etiqueta e JOIN nodo_catalogo n"
                + " ON n.id = e.habilidad_id WHERE e.publicacion_id = p.id AND LOWER(n.nombre) LIKE :texto)"
                + " THEN " + PESO_ETIQUETA + " ELSE 0 END"
                + " + CASE WHEN LOWER(p.descripcion) LIKE :texto THEN " + PESO_DESCRIPCION + " ELSE 0 END)";
    }

    private String ordenamiento(OrdenBusqueda orden) {
        return switch (orden) {
            case RELEVANCIA -> "relevancia DESC, p.fecha_creacion DESC, p.id DESC";
            case MEJOR_REPUTACION -> "5 DESC, p.fecha_creacion DESC, p.id DESC";
            case MENOR_PRECIO -> "p.precio_base ASC, p.id ASC";
            case MAS_RECIENTE -> "p.fecha_creacion DESC, p.id DESC";
        };
    }

    private Map<Long, Set<Habilidad>> etiquetasDe(List<Long> publicaciones) {
        Map<Long, Set<Habilidad>> etiquetas = new HashMap<>();
        if (publicaciones.isEmpty()) {
            return etiquetas;
        }
        Query consulta = entityManager().createNativeQuery("""
                SELECT e.publicacion_id, n.id, n.nombre FROM publicacion_etiqueta e
                JOIN nodo_catalogo n ON n.id = e.habilidad_id WHERE e.publicacion_id IN (:ids)""");
        consulta.setParameter("ids", publicaciones);
        for (Object[] fila : filas(consulta)) {
            etiquetas.computeIfAbsent(((Number) fila[0]).longValue(), id -> new HashSet<>())
                    .add(new Habilidad(((Number) fila[1]).longValue(), (String) fila[2]));
        }
        return etiquetas;
    }

    private PublicacionResumen aResumen(Object[] fila) {
        return new PublicacionResumen(((Number) fila[0]).longValue(), (String) fila[1], (String) fila[2],
                SkillCoins.de(((Number) fila[3]).longValue()), redondear(((Number) fila[4]).doubleValue()));
    }

    private boolean tieneTexto(FiltroBusqueda filtro) {
        return filtro.texto() != null && !filtro.texto().isBlank();
    }

    private String patron(String texto) {
        String escapado = texto.trim().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escapado + "%";
    }

    private double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

    private void asignar(Query consulta, Map<String, Object> parametros) {
        parametros.forEach(consulta::setParameter);
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> filas(Query consulta) {
        return new ArrayList<>(consulta.getResultList());
    }
}
