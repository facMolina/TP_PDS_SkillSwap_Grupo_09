package ar.edu.uade.skillswap.aplicacion.puerto;

import java.util.Optional;

import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.aplicacion.comun.SolicitudPagina;
import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.dominio.billetera.Billetera;
import ar.edu.uade.skillswap.dominio.billetera.MovimientoCreditos;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public interface BilleteraRepositorio {

    Billetera guardar(Billetera billetera);

    Optional<Billetera> buscarPorUsuario(UsuarioId usuario);

    Pagina<MovimientoCreditos> listarMovimientos(UsuarioId usuario, SolicitudPagina pagina);

    default Billetera obtenerPorUsuario(UsuarioId usuario) {
        return buscarPorUsuario(usuario)
                .orElseThrow(() -> new RecursoNoEncontrado("No existe la billetera de " + usuario.valor()));
    }
}
