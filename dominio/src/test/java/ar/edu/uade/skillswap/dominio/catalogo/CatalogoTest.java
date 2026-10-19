package ar.edu.uade.skillswap.dominio.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

class CatalogoTest {

    private final Categoria tecnologia = new Categoria(1L, "Tecnología");
    private final Categoria programacion = new Categoria(2L, "Programación");
    private final Habilidad java = new Habilidad(10L, "Java");
    private final Habilidad diseno = new Habilidad(11L, "Diseño");

    CatalogoTest() {
        tecnologia.agregar(programacion);
        programacion.agregar(java);
    }

    @Test
    void unaCategoriaIncluyeASusDescendientes() {
        assertTrue(tecnologia.incluye(programacion));
        assertTrue(tecnologia.incluye(java));
        assertTrue(programacion.incluye(java));
    }

    @Test
    void unaCategoriaNoIncluyeNodosAjenos() {
        assertFalse(programacion.incluye(tecnologia));
        assertFalse(tecnologia.incluye(diseno));
    }

    @Test
    void unaCategoriaSeIncluyeASiMisma() {
        assertTrue(programacion.incluye(programacion));
    }

    @Test
    void unaHabilidadSoloSeIncluyeASiMisma() {
        assertTrue(java.incluye(java));
        assertFalse(java.incluye(diseno));
    }

    @Test
    void quitarUnHijoLoExcluyeDelArbol() {
        programacion.quitar(java);

        assertFalse(tecnologia.incluye(java));
    }

    @Test
    void idsIncluidosReunePropioYDescendientes() {
        assertEquals(Set.of(1L, 2L, 10L), tecnologia.idsIncluidos());
        assertEquals(Set.of(2L, 10L), programacion.idsIncluidos());
        assertEquals(Set.of(10L), java.idsIncluidos());
    }
}
