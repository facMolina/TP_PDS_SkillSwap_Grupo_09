package ar.edu.uade.skillswap.infraestructura.persistencia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import ar.edu.uade.skillswap.aplicacion.casouso.billetera.ConsultaBilletera;
import ar.edu.uade.skillswap.aplicacion.casouso.contratacion.ContratacionVista;
import ar.edu.uade.skillswap.aplicacion.error.ConflictoDeConcurrencia;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.error.CupoAgotado;
import ar.edu.uade.skillswap.dominio.error.ErrorDeDominio;
import ar.edu.uade.skillswap.dominio.perfil.TipoPerfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.infraestructura.soporte.PruebaConBaseDatos;

class ConcurrenciaPersistenciaTest extends PruebaConBaseDatos {

    @RepeatedTest(10)
    void dosAceptacionesSimultaneasDeUnCupoUnicoSoloTienenUnGanador() throws Exception {
        registrarProveedorYCliente();
        registrar(OTRO, TipoPerfil.PERSONA);
        Long publicacion = publicacionBasica(1);
        ContratacionVista primera = solicitar(CLIENTE, publicacion, F1);
        ContratacionVista segunda = solicitar(OTRO, publicacion, F2);

        List<Object> resultados = enParalelo(List.of(
                () -> aceptar(primera.id()),
                () -> aceptar(segunda.id())));

        assertEquals(1, resultados.stream().filter(r -> r instanceof ContratacionVista).count());
        Object fallo = resultados.stream().filter(r -> !(r instanceof ContratacionVista)).findFirst().orElseThrow();
        assertInstanceOf(CupoAgotado.class, fallo);
        var recuperada = app.aplicacion.transaccion.consultar(() -> app.puertos.publicaciones().obtenerPorId(publicacion));
        assertEquals(1, recuperada.getCuposTomados());
    }

    @RepeatedTest(10)
    void dosPagosSimultaneosDeLaMismaContratacionDebitanUnaSolaVez() throws Exception {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(3);
        ContratacionVista contratacion = solicitar(CLIENTE, publicacion, F1);
        aceptar(contratacion.id());

        List<Object> resultados = enParalelo(List.of(
                () -> pagar(CLIENTE, contratacion.id()),
                () -> pagar(CLIENTE, contratacion.id())));

        assertEquals(1, resultados.stream().filter(r -> r instanceof ContratacionVista).count());
        Object fallo = resultados.stream().filter(r -> !(r instanceof ContratacionVista)).findFirst().orElseThrow();
        assertTrue(fallo instanceof ErrorDeDominio || fallo instanceof ConflictoDeConcurrencia, fallo.toString());
        assertEquals(SkillCoins.de(900),
                app.aplicacion.consultarBilletera.ejecutar(new ConsultaBilletera(CLIENTE, null)).saldo());
    }

    @Test
    void guardarUnaPublicacionConVersionVencidaSeInformaComoConflicto() throws Exception {
        registrarProveedorYCliente();
        Long publicacion = publicacionBasica(3);

        assertThrows(ConflictoDeConcurrencia.class, () -> app.puertos.unidadDeTrabajo().ejecutar(() -> {
            Publicacion leida = app.puertos.publicaciones().obtenerPorId(publicacion);
            modificarDesdeOtraTransaccion(publicacion);
            leida.reservarCupo(F1);
            return app.puertos.publicaciones().guardar(leida);
        }));
    }

    private void modificarDesdeOtraTransaccion(Long publicacion) {
        ExecutorService ejecutor = Executors.newSingleThreadExecutor();
        try {
            ejecutor.submit(() -> app.puertos.unidadDeTrabajo().ejecutar(() -> {
                Publicacion otra = app.puertos.publicaciones().obtenerPorId(publicacion);
                otra.reservarCupo(F2);
                return app.puertos.publicaciones().guardar(otra);
            })).get();
        } catch (InterruptedException | ExecutionException error) {
            throw new IllegalStateException(error);
        } finally {
            ejecutor.shutdownNow();
        }
    }

    private List<Object> enParalelo(List<Callable<Object>> tareas) throws Exception {
        ExecutorService ejecutor = Executors.newFixedThreadPool(tareas.size());
        CyclicBarrier largada = new CyclicBarrier(tareas.size());
        try {
            List<Future<Object>> futuros = new ArrayList<>();
            for (Callable<Object> tarea : tareas) {
                futuros.add(ejecutor.submit(() -> {
                    largada.await();
                    return tarea.call();
                }));
            }
            List<Object> resultados = new ArrayList<>();
            for (Future<Object> futuro : futuros) {
                resultados.add(resultado(futuro));
            }
            return resultados;
        } finally {
            ejecutor.shutdownNow();
        }
    }

    private Object resultado(Future<Object> futuro) throws InterruptedException {
        try {
            return futuro.get();
        } catch (ExecutionException error) {
            return error.getCause();
        }
    }
}
