package ar.edu.uade.skillswap.aplicacion.puerto;

import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public interface PerfilRepositorio {

    Perfil guardar(Perfil perfil);

    Optional<Perfil> buscarPorUsuario(UsuarioId usuario);

    boolean existePorUsuario(UsuarioId usuario);

    default Perfil obtenerPorUsuario(UsuarioId usuario) {
        return buscarPorUsuario(usuario)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe el perfil " + usuario.valor()));
    }
}
