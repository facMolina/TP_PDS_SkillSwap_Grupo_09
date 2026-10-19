package ar.edu.uade.skillswap.dominio.recomendacion;

import ar.edu.uade.skillswap.dominio.perfil.Perfil;

public interface EstrategiaRecomendacion {

    double puntuar(Perfil perfil, CandidataRecomendacion candidata);
}
