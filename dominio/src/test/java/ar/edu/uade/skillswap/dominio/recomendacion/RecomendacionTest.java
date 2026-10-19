package ar.edu.uade.skillswap.dominio.recomendacion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.dominio.Fixtures;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

class RecomendacionTest {

    private final Perfil perfil = Fixtures.perfilCliente();

    RecomendacionTest() {
        perfil.definirHabilidades(Set.of(Fixtures.JAVA));
        perfil.definirIntereses(Set.of(Fixtures.SQL));
    }

    @Test
    void laAfinidadCuentaLasEtiquetasEnComun() {
        CandidataRecomendacion ambas = new CandidataRecomendacion(1L, Set.of(Fixtures.JAVA, Fixtures.SQL), 0, 0);
        CandidataRecomendacion una = new CandidataRecomendacion(2L, Set.of(Fixtures.JAVA), 0, 0);
        CandidataRecomendacion ninguna = new CandidataRecomendacion(3L, Set.of(), 0, 0);
        EstrategiaRecomendacion estrategia = new RecomendacionPorAfinidad();

        assertEquals(2, estrategia.puntuar(perfil, ambas));
        assertEquals(1, estrategia.puntuar(perfil, una));
        assertEquals(0, estrategia.puntuar(perfil, ninguna));
    }

    @Test
    void laPopularidadUsaLasSolicitudesRecientes() {
        CandidataRecomendacion popular = new CandidataRecomendacion(1L, Set.of(), 12, 0);
        EstrategiaRecomendacion estrategia = new RecomendacionPorPopularidad();

        assertEquals(12, estrategia.puntuar(perfil, popular));
    }

    @Test
    void lasEstrategiasPuntuanDistintoLaMismaCandidata() {
        CandidataRecomendacion candidata = new CandidataRecomendacion(1L, Set.of(Fixtures.JAVA), 7, 0);

        assertEquals(1, new RecomendacionPorAfinidad().puntuar(perfil, candidata));
        assertEquals(7, new RecomendacionPorPopularidad().puntuar(perfil, candidata));
    }
}
