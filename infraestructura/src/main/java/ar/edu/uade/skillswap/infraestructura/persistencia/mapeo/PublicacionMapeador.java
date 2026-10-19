package ar.edu.uade.skillswap.infraestructura.persistencia.mapeo;

import java.util.List;

import ar.edu.uade.skillswap.dominio.cancelacion.TipoPoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.comun.FranjaHoraria;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.precio.Promocion;
import ar.edu.uade.skillswap.dominio.precio.PromocionMontoFijo;
import ar.edu.uade.skillswap.dominio.precio.PromocionPorcentual;
import ar.edu.uade.skillswap.dominio.publicacion.DatosPublicacion;
import ar.edu.uade.skillswap.dominio.publicacion.FranjaDisponible;
import ar.edu.uade.skillswap.dominio.publicacion.InstantaneaPublicacion;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.FranjaDisponibleEmbebida;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.NodoCatalogoEntidad;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.PromocionEmbebida;
import ar.edu.uade.skillswap.infraestructura.persistencia.entidad.PublicacionEntidad;
import jakarta.persistence.EntityManager;

public final class PublicacionMapeador {

    private PublicacionMapeador() {
    }

    public static Publicacion aDominio(PublicacionEntidad e) {
        DatosPublicacion datos = new DatosPublicacion(TipoPublicacion.valueOf(e.tipo), e.titulo, e.descripcion,
                e.categoria == null ? null : CatalogoMapeador.aCategoria(e.categoria),
                e.modalidad == null ? null : Modalidad.valueOf(e.modalidad), e.duracionMinutos, e.cupo,
                e.precioBase == null ? null : SkillCoins.de(e.precioBase),
                CatalogoMapeador.aHabilidades(e.etiquetas), TipoPoliticaCancelacion.valueOf(e.politicaCancelacion));
        List<FranjaDisponible> franjas = e.franjas.stream()
                .map(f -> new FranjaDisponible(new FranjaHoraria(f.inicio, f.fin), f.ocupada))
                .toList();
        List<Promocion> promociones = e.promociones.stream().map(PublicacionMapeador::aPromocion).toList();
        return Publicacion.reconstruir(new InstantaneaPublicacion(e.id, new UsuarioId(e.autorId), datos,
                e.cuposTomados, franjas, promociones, e.estado, TransicionMapeador.aDominio(e.historial),
                e.fechaCreacion, e.version));
    }

    public static PublicacionEntidad nueva(Publicacion p) {
        PublicacionEntidad e = new PublicacionEntidad();
        e.autorId = p.getAutorId().valor();
        e.tipo = p.getTipo().name();
        e.fechaCreacion = p.getFechaCreacion();
        return e;
    }

    public static void copiar(Publicacion p, PublicacionEntidad e, EntityManager entityManager) {
        e.titulo = p.getTitulo();
        e.descripcion = p.getDescripcion();
        e.categoria = p.getCategoria() == null
                ? null : entityManager.getReference(NodoCatalogoEntidad.class, p.getCategoria().id());
        e.modalidad = p.getModalidad() == null ? null : p.getModalidad().name();
        e.duracionMinutos = p.getDuracionMinutos();
        e.cupo = p.getCupo();
        e.cuposTomados = p.getCuposTomados();
        e.precioBase = p.getPrecioBase() == null ? null : p.getPrecioBase().monto();
        e.politicaCancelacion = p.getTipoPoliticaCancelacion().name();
        e.estado = p.getNombreEstado();
        CatalogoMapeador.sincronizar(e.etiquetas, p.getEtiquetas(), entityManager);
        e.franjas.clear();
        p.getFranjas().forEach(f -> e.franjas.add(aEmbebida(f)));
        e.promociones.clear();
        p.getPromociones().forEach(promocion -> e.promociones.add(aEmbebida(promocion)));
        TransicionMapeador.copiar(p.getHistorial().getTransiciones(), e.historial);
    }

    private static FranjaDisponibleEmbebida aEmbebida(FranjaDisponible f) {
        FranjaDisponibleEmbebida e = new FranjaDisponibleEmbebida();
        e.inicio = f.getFranja().inicio();
        e.fin = f.getFranja().fin();
        e.ocupada = f.estaOcupada();
        return e;
    }

    private static PromocionEmbebida aEmbebida(Promocion promocion) {
        PromocionEmbebida e = new PromocionEmbebida();
        e.vigenteDesde = promocion.getVigenteDesde();
        e.vigenteHasta = promocion.getVigenteHasta();
        if (promocion instanceof PromocionPorcentual porcentual) {
            e.tipo = PromocionEmbebida.PORCENTAJE;
            e.porcentaje = porcentual.getPorcentaje();
        } else if (promocion instanceof PromocionMontoFijo fija) {
            e.tipo = PromocionEmbebida.MONTO_FIJO;
            e.monto = fija.getMonto().monto();
        } else {
            throw new IllegalArgumentException("Tipo de promoción no soportado: " + promocion.getClass());
        }
        return e;
    }

    private static Promocion aPromocion(PromocionEmbebida e) {
        if (PromocionEmbebida.PORCENTAJE.equals(e.tipo)) {
            return new PromocionPorcentual(e.porcentaje, e.vigenteDesde, e.vigenteHasta);
        }
        return new PromocionMontoFijo(SkillCoins.de(e.monto), e.vigenteDesde, e.vigenteHasta);
    }
}
