package ar.edu.uade.skillswap.infraestructura.persistencia;

public record ConfiguracionBaseDatos(String url, String usuario, String clave) {

    public static final String VARIABLE_URL = "SKILLSWAP_DB_URL";
    public static final String VARIABLE_USUARIO = "SKILLSWAP_DB_USER";
    public static final String VARIABLE_CLAVE = "SKILLSWAP_DB_PASSWORD";

    public static ConfiguracionBaseDatos desdeEntorno() {
        return new ConfiguracionBaseDatos(
                requerida(VARIABLE_URL), requerida(VARIABLE_USUARIO), requerida(VARIABLE_CLAVE));
    }

    private static String requerida(String variable) {
        String valor = System.getenv(variable);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno " + variable);
        }
        return valor;
    }
}
