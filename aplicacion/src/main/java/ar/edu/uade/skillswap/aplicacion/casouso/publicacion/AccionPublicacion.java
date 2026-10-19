package ar.edu.uade.skillswap.aplicacion.casouso.publicacion;

import ar.edu.uade.skillswap.dominio.comun.ContextoAccion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

public enum AccionPublicacion {
    PUBLICAR(false) {
        @Override
        public void aplicar(Publicacion publicacion, ContextoAccion ctx, String motivo) {
            publicacion.publicar(ctx);
        }
    },
    PAUSAR(false) {
        @Override
        public void aplicar(Publicacion publicacion, ContextoAccion ctx, String motivo) {
            publicacion.pausar(ctx);
        }
    },
    REANUDAR(false) {
        @Override
        public void aplicar(Publicacion publicacion, ContextoAccion ctx, String motivo) {
            publicacion.reanudar(ctx);
        }
    },
    CERRAR(true) {
        @Override
        public void aplicar(Publicacion publicacion, ContextoAccion ctx, String motivo) {
            publicacion.cerrar(ctx);
        }
    },
    CANCELAR(true) {
        @Override
        public void aplicar(Publicacion publicacion, ContextoAccion ctx, String motivo) {
            publicacion.cancelar(ctx, motivo);
        }
    };

    private final boolean retiraDeCirculacion;

    AccionPublicacion(boolean retiraDeCirculacion) {
        this.retiraDeCirculacion = retiraDeCirculacion;
    }

    public boolean retiraDeCirculacion() {
        return retiraDeCirculacion;
    }

    public abstract void aplicar(Publicacion publicacion, ContextoAccion ctx, String motivo);
}
