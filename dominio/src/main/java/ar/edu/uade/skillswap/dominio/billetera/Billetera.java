package ar.edu.uade.skillswap.dominio.billetera;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import ar.edu.uade.skillswap.dominio.comun.Identificable;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.UsuarioId;
import ar.edu.uade.skillswap.dominio.comun.Versionado;
import ar.edu.uade.skillswap.dominio.error.SaldoInsuficiente;

public class Billetera implements Identificable, Versionado {

    private Long id;
    private final UsuarioId usuarioId;
    private SkillCoins saldo;
    private long version;
    private final List<MovimientoCreditos> movimientosPendientes = new ArrayList<>();

    private Billetera(UsuarioId usuarioId, SkillCoins saldo) {
        this.usuarioId = usuarioId;
        this.saldo = saldo;
    }

    public static Billetera abrir(UsuarioId usuarioId, SkillCoins saldoInicial, Instant ahora) {
        Billetera billetera = new Billetera(usuarioId, saldoInicial);
        billetera.movimientosPendientes.add(
                new MovimientoCreditos(TipoMovimiento.SALDO_INICIAL, saldoInicial, saldoInicial, "alta de perfil", ahora));
        return billetera;
    }

    public static Billetera reconstruir(Long id, UsuarioId usuarioId, SkillCoins saldo, long version) {
        Billetera billetera = new Billetera(usuarioId, saldo);
        billetera.id = id;
        billetera.version = version;
        return billetera;
    }

    public void acreditar(SkillCoins monto, TipoMovimiento tipo, String referencia, Instant ahora) {
        saldo = saldo.sumar(monto);
        movimientosPendientes.add(new MovimientoCreditos(tipo, monto, saldo, referencia, ahora));
    }

    public void debitar(SkillCoins monto, TipoMovimiento tipo, String referencia, Instant ahora) {
        if (!tieneSaldoPara(monto)) {
            throw new SaldoInsuficiente("El saldo no alcanza para cubrir " + monto.monto() + " SkillCoins");
        }
        saldo = saldo.restar(monto);
        movimientosPendientes.add(new MovimientoCreditos(tipo, monto, saldo, referencia, ahora));
    }

    public boolean tieneSaldoPara(SkillCoins monto) {
        return !saldo.esMenorQue(monto);
    }

    public List<MovimientoCreditos> extraerMovimientos() {
        List<MovimientoCreditos> extraidos = List.copyOf(movimientosPendientes);
        movimientosPendientes.clear();
        return extraidos;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void asignarId(Long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void fijarVersion(long version) {
        this.version = version;
    }

    public UsuarioId getUsuarioId() {
        return usuarioId;
    }

    public SkillCoins getSaldo() {
        return saldo;
    }
}
