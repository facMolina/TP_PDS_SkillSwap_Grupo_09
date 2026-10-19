package ar.edu.uade.skillswap.dominio.comun;

public interface Versionado {

    long getVersion();

    void fijarVersion(long version);
}
