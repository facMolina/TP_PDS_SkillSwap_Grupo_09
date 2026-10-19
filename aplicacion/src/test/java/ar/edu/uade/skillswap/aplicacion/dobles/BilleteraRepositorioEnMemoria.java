package ar.edu.uade.skillswap.aplicacion.dobles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.puerto.BilleteraRepositorio;
import ar.edu.uade.skillswap.dominio.billetera.Billetera;
import ar.edu.uade.skillswap.dominio.billetera.MovimientoCreditos;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public class BilleteraRepositorioEnMemoria implements BilleteraRepositorio {

    private final Map<UsuarioId, Billetera> almacen = new LinkedHashMap<>();
    private final Map<UsuarioId, List<MovimientoCreditos>> movimientos = new HashMap<>();
    private long secuencia;

    @Override
    public Billetera guardar(Billetera billetera) {
        if (billetera.getId() == null) {
            billetera.asignarId(++secuencia);
        }
        billetera.fijarVersion(billetera.getVersion() + 1);
        almacen.put(billetera.getUsuarioId(), billetera);
        movimientos.computeIfAbsent(billetera.getUsuarioId(), usuario -> new ArrayList<>())
                .addAll(billetera.extraerMovimientos());
        return billetera;
    }

    @Override
    public Optional<Billetera> buscarPorUsuario(UsuarioId usuario) {
        return Optional.ofNullable(almacen.get(usuario));
    }

    @Override
    public Pagina<MovimientoCreditos> listarMovimientos(UsuarioId usuario, SolicitudPagina pagina) {
        return Pagina.de(movimientos.getOrDefault(usuario, List.of()), pagina);
    }

    public List<MovimientoCreditos> movimientosDe(UsuarioId usuario) {
        return List.copyOf(movimientos.getOrDefault(usuario, List.of()));
    }
}
