package ar.edu.uade.skillswap.aplicacion.dobles;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class PerfilRepositorioEnMemoria implements PerfilRepositorio {

    private final Map<UsuarioId, Perfil> almacen = new LinkedHashMap<>();
    private long secuencia;

    @Override
    public Perfil guardar(Perfil perfil) {
        if (perfil.getId() == null) {
            perfil.asignarId(++secuencia);
        }
        almacen.put(perfil.getUsuarioId(), perfil);
        return perfil;
    }

    @Override
    public Optional<Perfil> buscarPorUsuario(UsuarioId usuario) {
        return Optional.ofNullable(almacen.get(usuario));
    }

    @Override
    public boolean existePorUsuario(UsuarioId usuario) {
        return almacen.containsKey(usuario);
    }
}
