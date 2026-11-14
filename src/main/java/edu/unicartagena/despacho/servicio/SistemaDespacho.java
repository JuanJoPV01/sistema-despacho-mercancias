package edu.unicartagena.despacho.servicio;

import edu.unicartagena.despacho.comun.ValidacionException;
import edu.unicartagena.despacho.modelo.EstadoSolicitud;

public final class SistemaDespacho {

    private final CatalogoProductos catalogo = new CatalogoProductos();
    private final RegistroDestinos destinos = new RegistroDestinos();
    private final GestorDespachos despachos = new GestorDespachos(catalogo, destinos);

    public CatalogoProductos catalogo() {
        return catalogo;
    }

    public RegistroDestinos destinos() {
        return destinos;
    }

    public GestorDespachos despachos() {
        return despachos;
    }

    public void cargarDatosDemostracion() throws ValidacionException {
        catalogo.limpiar();
        destinos.limpiar();
        despachos.limpiar();
        DatosDemostracion.cargar(this);
    }

    public ResumenColecciones resumen() {
        return new ResumenColecciones(
                despachos.cantidad(),
                despachos.cantidadIndiceCodigos(),
                destinos.cantidad(),
                catalogo.cantidad(),
                despachos.cantidadDestinosConDespachos(),
                despachos.cantidadEnCola(),
                despachos.cantidadPorEstado(EstadoSolicitud.PENDIENTE),
                despachos.cantidadPorEstado(EstadoSolicitud.DESPACHADA),
                despachos.cantidadPorEstado(EstadoSolicitud.CANCELADA));
    }
}
