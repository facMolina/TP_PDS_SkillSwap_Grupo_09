package ar.edu.uade.skillswap.aplicacion.configuracion;

import ar.edu.uade.skillswap.aplicacion.puerto.BilleteraRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.CalificacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.CatalogoRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.ConsultaPublicaciones;
import ar.edu.uade.skillswap.aplicacion.puerto.ContratacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.EnviadorEmail;
import ar.edu.uade.skillswap.aplicacion.puerto.EnviadorPush;
import ar.edu.uade.skillswap.aplicacion.puerto.NotificacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PerfilRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.PublicacionRepositorio;
import ar.edu.uade.skillswap.aplicacion.puerto.ServicioIdentidad;
import ar.edu.uade.skillswap.aplicacion.puerto.UnidadDeTrabajo;

public record Puertos(
        PerfilRepositorio perfiles,
        PublicacionRepositorio publicaciones,
        ContratacionRepositorio contrataciones,
        BilleteraRepositorio billeteras,
        CalificacionRepositorio calificaciones,
        NotificacionRepositorio notificaciones,
        CatalogoRepositorio catalogo,
        ConsultaPublicaciones consulta,
        ServicioIdentidad identidad,
        EnviadorEmail email,
        EnviadorPush push,
        UnidadDeTrabajo unidadDeTrabajo) {
}
