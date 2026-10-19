package ar.edu.uade.skillswap.aplicacion.casouso.billetera;

import ar.edu.uade.skillswap.aplicacion.comun.Pagina;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

public record BilleteraVista(SkillCoins saldo, Pagina<MovimientoVista> movimientos) {
}
