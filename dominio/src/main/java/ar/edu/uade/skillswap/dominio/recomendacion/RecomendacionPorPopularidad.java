package ar.edu.uade.skillswap.dominio.recomendacion;

import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public class RecomendacionPorPopularidad implements EstrategiaRecomendacion {

    @Override
    public double puntuar(Perfil perfil, CandidataRecomendacion candidata) {
        return candidata.solicitudesRecientes();
    }
}
