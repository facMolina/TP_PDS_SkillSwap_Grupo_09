package ar.edu.uade.skillswap.aplicacion.notificacion;

import java.util.EnumMap;
import java.util.Map;

import ar.edu.uade.skillswap.dominio.perfil.CanalPreferido;

public class FabricaCanales {

    private final Map<CanalPreferido, CanalNotificacion> canales = new EnumMap<>(CanalPreferido.class);

    public FabricaCanales(CanalEmail canalEmail, CanalPush canalPush, CanalInterno canalInterno) {
        canales.put(CanalPreferido.EMAIL, canalEmail);
        canales.put(CanalPreferido.PUSH, canalPush);
        canales.put(CanalPreferido.INTERNA, canalInterno);
    }

    public CanalNotificacion crear(CanalPreferido canal) {
        return canales.get(canal);
    }
}
