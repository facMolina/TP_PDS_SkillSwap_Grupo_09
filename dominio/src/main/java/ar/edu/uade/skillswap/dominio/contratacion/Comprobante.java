package ar.edu.uade.skillswap.dominio.contratacion;

import java.time.Instant;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;

public record Comprobante(Long contratacionId, UsuarioId clienteId, UsuarioId proveedorId, SkillCoins monto,
                          Instant fecha) {
}
