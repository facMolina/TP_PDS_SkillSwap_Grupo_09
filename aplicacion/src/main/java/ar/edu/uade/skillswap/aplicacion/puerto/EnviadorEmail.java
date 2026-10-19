package ar.edu.uade.skillswap.aplicacion.puerto;

public interface EnviadorEmail {

    void enviarCorreo(String destino, String asunto, String cuerpo);
}
