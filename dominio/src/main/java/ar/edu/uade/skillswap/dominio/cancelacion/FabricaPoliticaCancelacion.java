package ar.edu.uade.skillswap.dominio.cancelacion;

public class FabricaPoliticaCancelacion {

    private final PoliticaCancelacion flexible;
    private final PoliticaCancelacion estricta;

    public FabricaPoliticaCancelacion(PoliticaCancelacion flexible, PoliticaCancelacion estricta) {
        this.flexible = flexible;
        this.estricta = estricta;
    }

    public static FabricaPoliticaCancelacion porDefecto() {
        return new FabricaPoliticaCancelacion(PoliticaFlexible.porDefecto(), PoliticaEstricta.porDefecto());
    }

    public PoliticaCancelacion crear(TipoPoliticaCancelacion tipo) {
        return switch (tipo) {
            case FLEXIBLE -> flexible;
            case ESTRICTA -> estricta;
        };
    }
}
