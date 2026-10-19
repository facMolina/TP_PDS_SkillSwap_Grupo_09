package ar.edu.uade.skillswap.aplicacion.evento;

import ar.edu.uade.skillswap.aplicacion.error.RecursoNoEncontrado;
import ar.edu.uade.skillswap.aplicacion.notificacion.CanalNotificacion;
import ar.edu.uade.skillswap.aplicacion.notificacion.FabricaCanales;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.evento.EventoContratacion;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class NotificadorDeEventos implements ObservadorEventos {

    private final PerfilRepositorio perfiles;
    private final FabricaCanales fabricaCanales;
    private final GeneradorMensajes generadorMensajes;

    public NotificadorDeEventos(PerfilRepositorio perfiles, FabricaCanales fabricaCanales,
                                GeneradorMensajes generadorMensajes) {
        this.perfiles = perfiles;
        this.fabricaCanales = fabricaCanales;
        this.generadorMensajes = generadorMensajes;
    }

    @Override
    public void alRecibir(EventoContratacion evento) {
        String mensaje = generadorMensajes.mensajePara(evento);
        for (UsuarioId destinatario : evento.destinatarios()) {
            Perfil perfil = perfiles.buscarPorUsuario(destinatario)
                    .orElseThrow(() -> new RecursoNoEncontrado("No existe el perfil " + destinatario.valor()));
            Notificacion notificacion = new Notificacion(destinatario, evento.tipo(), mensaje, evento.ocurridoEn());
            CanalNotificacion canal = fabricaCanales.crear(perfil.getCanalPreferido());
            canal.enviar(notificacion, perfil);
        }
    }
}
