package ar.edu.uade.skillswap.dominio.recomendacion;

import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class RecomendacionPorAfinidad implements EstrategiaRecomendacion {

    @Override
    public double puntuar(Perfil perfil, CandidataRecomendacion candidata) {
        return perfil.habilidadesEInteresesCombinados().stream()
                .filter(candidata.etiquetas()::contains)
                .count();
    }
}
