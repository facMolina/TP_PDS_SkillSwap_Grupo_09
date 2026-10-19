package ar.edu.uade.skillswap.infraestructura.externo;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServicioIdentidadSimulado {

    private final Set<String> cuentas = ConcurrentHashMap.newKeySet();
    private volatile String usuarioActivo;

    public ServicioIdentidadSimulado(Collection<String> cuentasIniciales) {
        cuentas.addAll(cuentasIniciales);
    }

    public void iniciarSesion(String identificador) {
        if (!existe(identificador)) {
            throw new IllegalArgumentException("La cuenta " + identificador + " no existe");
        }
        usuarioActivo = identificador;
    }

    public String usuarioActivo() {
        return usuarioActivo;
    }

    public boolean existe(String identificador) {
        return cuentas.contains(identificador);
    }
}
